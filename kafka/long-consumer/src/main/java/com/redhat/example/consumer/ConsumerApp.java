package com.redhat.example.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * ConsumerApp - a simple Kafka consumer application implementing the exactly once semantic.
 * - kafka cannot handle distributed transactions with an external system (e.g. DBMS)
 * - to achieve the exactly once semantic, a consumer does not rely on the built-in offset storage
 * - the results of the consumption are being stored in a relational database, storing the offset in the database as well can allow committing both the results and offset in a single transaction.
 */
public class ConsumerApp {

    final static public String TOPIC = "event";

    public static void main(String[] args) {

        System.out.println(configureProperties());

        try (Consumer<Integer, Integer> consumer = new KafkaConsumer<>(configureProperties())) {
            SaveOffsetsOnRebalance saveOffsetsOnRebalance = new SaveOffsetsOnRebalance(consumer);

            consumer.subscribe(Collections.singletonList(TOPIC), saveOffsetsOnRebalance);

            while (true) {
                System.out.println("Waiting for events...");

                ConsumerRecords<Integer, Integer> records = consumer.poll(Duration.ofSeconds(10));

                for (ConsumerRecord<Integer, Integer> record : records) {
                    // begin transaction
                    // process the record
                    System.out.println("Received - key: " + record.key() + ", value: " + record.value() + " offset: "
                            + record.offset());
                    // save the offset
                    saveOffsetsOnRebalance.save(record.partition(), record.offset());
                    // commit transaction
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Properties configureProperties() {
        Properties props = new Properties();

        // configure the bootstrap server
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getEnv("KAFKA_BOOTSTRAP_SERVERS", "localhost:19092"));

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "long-consumer-group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.LongDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.StringDeserializer");

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
}
