package com.redhat.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.redhat.example.model.KOffset;
import com.redhat.example.model.KRecord;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OffsetConsumer {

    @Transactional
    @Incoming("event")
    public void consume(ConsumerRecord<Long, String> record) {
        System.out.println("Consumed message with key " + record.key() + " and value " + record.value()
                + " with offset " + record.offset());
        KRecord krecord = new KRecord();
        krecord.key = record.key();
        krecord.value = record.value();
        krecord.persist();

        KOffset.findByIdOptional(record.topic(), record.partition())
                .ifPresentOrElse(
                        offset -> {
                            offset.offsetCount = record.offset();
                            offset.persist();
                        },
                        () -> {
                            KOffset offset = new KOffset();
                            offset.offsetCount = record.offset();
                            offset.topic = record.topic();
                            offset.partition = record.partition();
                            offset.persist();
                        });
    }
}