package com.redhat.example.consumer;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;

public class SaveOffsetsOnRebalance implements ConsumerRebalanceListener {

    // this hash map is used to store the offset for each partition and mimic the behavior of an external offset storage
    private static ConcurrentHashMap<Integer, Long> offsetMap = new ConcurrentHashMap<>();
    private Consumer<?, ?> consumer;

    public SaveOffsetsOnRebalance(Consumer<?, ?> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        // NOTHING
    }

    /**
     * This method is called when a new partition is assigned to the consumer.
     * It retrieves the offset from the external storage (simulated by the offsetMap), then it seeks to that offset + 1.
     */
    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        for (TopicPartition partition : partitions) {
            var offset = offsetMap.get(Integer.valueOf(partition.partition()));
            if (offset != null) {
                consumer.seek(partition, offset + 1);

                System.out.format("Partition %d seek offset: %d\n", partition.partition(), offset);
            }
        }
    }

    public void save(Integer partition, Long offset) {
        offsetMap.put(partition, offset);
    }
}
