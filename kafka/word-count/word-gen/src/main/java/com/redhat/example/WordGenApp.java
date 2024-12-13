package com.redhat.example;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class WordGenApp {

    final static public String TOPIC = "word-count-input";

    final static String[] CAPITAL_NAMES = {
            "Paris", "London", "Berlin", "Rome", "Madrid",
            "Moscow", "Tokyo", "Athens", "Beijing", "Cairo"
    };

    public static void main(String[] args) {

        var sleep = Long.parseLong(getEnv("GEN_SLEEP", "500"));

        // Kafka producer
        var producer = new KafkaProducer<Long, String>(configureProperties());

        long counter = 0;

        while (true) {

            try {

                var record = new ProducerRecord<>(
                        TOPIC,
                        counter++, CAPITAL_NAMES[(int)(counter % 10)]);

                var future = producer.send(record);

                printRecord(record, future);
                TimeUnit.MILLISECONDS.sleep(sleep);

            } catch (InterruptedException e) {
                e.printStackTrace();
                producer.close();
            }
        }
    }

    public static Properties configureProperties() {
        Properties props = new Properties();

        // configure the bootstrap server
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:19092"));

        // configure the key and value serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.LongSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringSerializer");

        return props;
    }

    private static String getEnv(String k, String v) {
        var val = System.getenv(k);
        if (val != null) {
            return val;
        } else {
            return v;
        }
    }

    private static void printRecord(ProducerRecord<?, ?> record, Future<RecordMetadata> future) {

        int partition = 0;

        try {
            var recordMetadata = future.get();
            partition = recordMetadata.partition();
        } catch (Throwable t) {
            // NOTHING
        }

        System.out.print("Sent record:");
        System.out.print("\tTopic = " + record.topic());
        System.out.print("\tPartition = " + partition);
        System.out.print("\tKey = " + record.key());
        System.out.println("\tValue = " + record.value());
    }
}
