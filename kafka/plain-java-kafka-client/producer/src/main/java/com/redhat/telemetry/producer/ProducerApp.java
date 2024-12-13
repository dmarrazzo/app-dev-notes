package com.redhat.telemetry.producer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

public class ProducerApp {

    final static public String TOPIC = "telemetry";

    public static Properties configureProperties() {
        Properties props = new Properties();

        // configure the bootstrap server
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443");
        // "dm-bus-cbppg-n-ocpgubcoc--g.bf2.kafka.rhcloud.com:443");

        // configure the key and value serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.IntegerSerializer");

        // configure the SSL connection
        // props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                "/home/donato/git/appsvc-notes/plain-java-kafka-client/truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");

        // Plain authentication
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG,
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"f0e42480-47d4-47b6-ad03-b7c6c77702e4\" password=\"N2E2ymfpWRQRxJ3iK425lzeJcdLOB23u\" ;");

        return props;
    }

    public static void main(String[] args) {
        // Kafka producer
        Random random = new Random();
        Producer<Integer, Integer> producer = new KafkaProducer<>(configureProperties());

        ProducerRecord<Integer, Integer> record = null;

        for (int j = 0; j < 10; j++) {

            try {

                for (int i = 0; i < 10; i++) {
                    record = new ProducerRecord<>(
                            TOPIC,
                            i,
                            random.nextInt(100));

                    producer.send(record);
                    printRecord(record);
                }
                TimeUnit.SECONDS.sleep(1);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            producer.flush();
        }
        producer.close();
    }

    private static void printRecord(ProducerRecord<?, ?> record) {
        System.out.println("Sent record:");
        System.out.println("\tTopic = " + record.topic());
        System.out.println("\tPartition = " + record.partition());
        System.out.println("\tKey = " + record.key());
        System.out.println("\tValue = " + record.value());
    }
}
