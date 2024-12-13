# Mirror Maker 2

Use cases:

- Geo-replication
- Disaster recovery
- Feeding edge cluster into a central, aggregate cluster
- Cloud migration or hybrid cloud deployments
- Legal, technical, or compliance requirements

Key features:

- **Topic topology replication** same structure of the topic and partitions in the target Kafka cluster.
- **Consumer group offset synchronization**: to avoid consuming duplicated messages in the target cluster.
- **Dynamic detection of configuration changes** in source Kafka cluster and replicate in target Kafka cluster
- **Replication ACL rules** to manage access to source brokers and topics apply to target topics

MirrorMaker2 deploys a KafkaConnect cluster with a set of different Kafka Connectors

- **MirrorSourceConnector** is responsible for topics, data replication based on a replication policy, and other synchronization tasks such as ACL rules, renaming topics, etc.
- **MirrorCheckpointConnector** is responsible for track offset for consumer groups and synchronizing into the target Kafka cluster.
- **MirrorHeartbeatConnector** is responsible for checking the connectivity between clusters using a set of heartbeats and checks.

Useful links:

- https://kafka.apache.org/documentation/#georeplication 
- https://www.conflueant.io/events/kafka-summit-apac-2021/getting-up-to-speed-with-mirrormaker-2/ https://developers.redhat.com/articles/2021/12/15/how-use-mirrormaker-2-openshift-streams-apache-kafka#what_is_apache_kafka_mirrormaker_2_
- https://cloud.redhat.com/blog/deploying-monitoring-and-migrating-amq-streams
- https://strimzi.io/blog/2021/11/22/migrating-kafka-with-mirror-maker2/
- https://developers.redhat.com/articles/2021/12/15/how-use-mirrormaker-2-openshift-streams-apache-kafka#test_mirrormaker_s_synchronization

## Permissions for AMQ Stream Managed

Source:

- consumer group * allow all
- topic is * allow all

Target:

- kafka instance allow alter
- consumer group * allow all
- topic is * allow all

## Set up MM2

```sh
oc create secret generic source-client-secret --from-literal=client-secret=<Source Client Secret>
oc apply -f mm2.yaml
```

## Tips

```sh
oc create secret generic source-client-secret --from-literal=client-secret=<client-secret>
```

## Behavior

1. a consumer attached on the source cluster: receive messages generated after the start up
2. a consumer attached on the target cluster: receive messages generated after the start up
3. stop and start a consumer on the target cluster, the consumer gets the messages sent when it was offline