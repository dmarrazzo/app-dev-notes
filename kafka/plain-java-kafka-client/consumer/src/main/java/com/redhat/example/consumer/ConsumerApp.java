package com.redhat.example.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import com.redhat.example.producer.ProducerApp;

public class ConsumerApp {
    public static Properties configureProperties() {
        Properties producerProperties = ProducerApp.configureProperties();
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                producerProperties.getProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "total-connected-devices-cgroup");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerDeserializer");

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                producerProperties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG));
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                producerProperties.getProperty(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG));
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                producerProperties.getProperty(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG));

        // Plain authentication
        if (producerProperties.getProperty(SaslConfigs.SASL_MECHANISM) != null) {
            props.put(SaslConfigs.SASL_MECHANISM, producerProperties.getProperty(SaslConfigs.SASL_MECHANISM));
            props.put(SaslConfigs.SASL_JAAS_CONFIG, producerProperties.getProperty(SaslConfigs.SASL_JAAS_CONFIG));
        }
        return props;
    }

    public static void main(String[] args) {

        System.out.println(configureProperties());

        try (Consumer<Integer, Integer> consumer = new KafkaConsumer<>(configureProperties())) {
            consumer.subscribe(Collections.singletonList(ProducerApp.TOPIC));

            while (true) {
                System.out.println("Waiting for events...");

                ConsumerRecords<Integer, Integer> records = consumer.poll(Duration.ofMillis(10000));
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (ConsumerRecord<Integer, Integer> record : records) {
                    System.out.println("Received - key: " + record.key() + ", value: " + record.value());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
