package com.redhat.example;

import java.util.Collection;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;

import com.redhat.example.model.KOffset;

import io.smallrye.common.annotation.Identifier;
import io.smallrye.reactive.messaging.kafka.KafkaConsumerRebalanceListener;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
@Identifier("event-consumer.rebalancer")
public class KafkaRebalanceListener implements KafkaConsumerRebalanceListener {

    private static final Logger LOG = Logger.getLogger(KafkaRebalanceListener.class.getName());

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
        LOG.info("start");

        for (TopicPartition partition : partitions) {
            KOffset.findByIdOptional(partition.topic(), partition.partition()).ifPresentOrElse(
                    offset -> {
                        LOG.fine("seek topic: " + offset.topic + " partition: " + offset.partition + " offset: "
                                + offset.offsetCount + 1);
                        consumer.seek(partition, offset.offsetCount + 1);
                    },
                    () -> {
                        consumer.seekToBeginning(partitions);
                    });
        }
    }

}
