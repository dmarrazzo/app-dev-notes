package com.redhat.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

public class WordCountApp {

    public static void main(String[] args) {
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getEnv("KAFKA_BOOTSTRAP_SERVERS", "127.0.0.1:19092"));
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // config.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5000);
        // config.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/test");

        // date format pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        StreamsBuilder builder = new StreamsBuilder();
        // 1 - stream from Kafka

        KStream<Long, String> textLines = builder.stream("word-count-input");

        KTable<String, Long> wordCounts = textLines
                // 2 - map values to lowercase
                .mapValues(
                    value -> value.toLowerCase())
                // 3 - change the record key
                .selectKey((key,word) -> {
                    // Format the date and time
                    String formattedDateTime = LocalDateTime.now().format(formatter);

                    // Print the formatted date and time
                    System.out.println("###" + formattedDateTime + " word: " + word + " counter: " + key);
                    return word;
                })
                // 4 - group by key before aggregation (since the key type is changed -> new serdes)
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                // 5 - count occurrences
                .count(Named.as("Counts"));

        // 6 - to in order to write the results back to kafka
        wordCounts.toStream()
                .to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

        final Topology topology = builder.build();

        System.out.println("### "+topology.describe());

        final KafkaStreams streams = new KafkaStreams(topology, config);

        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);

    }

    private static String getEnv(String k, String v) {
        var val = System.getenv(k);
        if (val != null) {
            return val;
        } else {
            return v;
        }
    }
}
