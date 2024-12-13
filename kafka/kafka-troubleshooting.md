## Message rejected not in-sync

org.apache.kafka.common.errors.NotEnoughReplicasException: Messages are
rejected since there are fewer in-sync replicas than required.

https://access.redhat.com/solutions/7010782

A broker issue will show up as spikes in the cluster metrics and is
frequently related to slow or failing storage devices or compute
restraints from other processes. If there is no issue at the OS or
hardware level, then the cause is almost always an imbalance in the
load of the Kafka cluster.

In a balanced cluster partitions, leaders and IO metrics should be
even across all brokers. It is highly recommended to use an external
tool like Cruise Control for keeping the cluster balanced at all time.

These are some of the JMX metrics you can check for each broker:

kafka.server:type=ReplicaManager,name=PartitionCount
kafka.server:type=ReplicaManager,name=LeaderCount
kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec
kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec

kafka.server:type=KafkaRequestHandlerPool,name=RequestHandlerAvgIdlePercent
# attributes: OneMinuteRate, FifteenMinuteRate

Then, you can use the `kafka-topics.sh` tool to get a list of
under-replicated partitions and identify the problematic brokers.

bin/kafka-topics.sh --bootstrap-server :9092 --describe
--under-replicated-partitions

If the number of under-replicated partitions is fluctuating or many
brokers shows **high request latency**, this typically indicates a
**performance issue** in the cluster. A steady (unchanging) number of
under-replicated partitions reported by many of the brokers in a
cluster normally indicates that one of the brokers in the cluster is
offline.

If the cluster is balanced, you may find that you have a client that
has changed its request pattern and is now causing problems.
In this case, the solutions available are either to reduce the load to
the cluster or increase the number of brokers.
