AMQ Streams / Kafka 
=======================================

## Exploration topics / Questions

- Kafka connect? Is it a different product? real?
- Zookeeper removal?
- How to access to kafka from outside?
- Kafka Connect: is it used? Camel direct?
- record and message can have different meanings

## Resource for Learning

[Learning path](https://developers.redhat.com/products/red-hat-openshift-streams-for-apache-kafka/hello-kafka)

https://developers.redhat.com/articles/2022/02/14/serialize-debezium-events-apache-avro-and-openshift-service-registry?sc_cid=7013a000002qKlHAAU

https://developers.redhat.com/articles/2021/10/11/get-started-openshift-service-registry

[Finding Workload Balance: Cruise Control for Kafka on Kubernetes-Paolo Patierno Kyle Liberti Red Hat](https://www.youtube.com/watch?v=Ox11Wo1RANI)

[AMQ Streams Sessions For GSS](https://learning.redhat.com/enrol/index.php?id=3096)

[https://videos.learning.redhat.com/](https://videos.learning.redhat.com/)

search for kafka enablement

[Developing Event-Driven Applications with Apache Kafka and Red Hat AMQ Streams](https://role.rhu.redhat.com/rol-rhu/app/courses/ad482ea-1.8/pages/pr01)

[hybrid cloud with kafka in healthcare](https://dzone.com/articles/legacy-modernization-and-hybrid-cloud-with-kafka-i)

https://developer.confluent.io/tutorials/#explore-top-use-cases

## Production Ready

### Disaster recovery

It's possible to run Kafka cluster on stretched OCP cluster (not supported). 
Otherwise Kafka clusters can run on different clusters with mirroring between them. But mirroring does not offer any guarantees since it is asynchronous

### Message reliability

For data durability, you should also set `min.insync.replicas` in your topic configuration and message delivery acknowledgments using `acks=all` in your producer configuration.

If you are using application flush management, setting lower flush thresholds might be appropriate if you are using faster disks.

- On the producer side: `ack=all` (or -1)
- On the broker side: `min.insync.replica=2`

[Make your Kafka cluster production-ready: Reliability](https://www.youtube.com/watch?v=xyfL0IUCLsg)

In Quarkus, to ensure that message is written at least on one replica

```
mp.messaging.outgoing.event-out.acks=1
mp.messaging.outgoing.event-out.waitForWriteCompletion=true
```

- taint the nodes
- label the nodes
- configure the broker

  - template > pod > toleration > key "dedicated" value "kafka"
  

[Make your Kafka cluster production-ready: Dedicated Nodes and Affinity](https://www.youtube.com/watch?v=u1n9rg7pELo)

### Transactions

https://strimzi.io/blog/2023/05/03/kafka-transactions/

### Storage

Block storage is required. File storage, such as NFS, does not work with Kafka.

Choose from one of the following options for your block storage:

- Cloud-based block storage solutions, such as Amazon Elastic Block Store (EBS)
- Local persistent volumes
- Storage Area Network (SAN) volumes accessed by a protocol such as Fibre Channel or iSCSI

> I was imaging the following policy: if the PV is not available (given a timeout), in order to restore the cluster performances a new broker is launched with an empty disk to re-sync

No, this is not how it works. Not having the broker available might mean that the performance drops. But its not like by replacing it with new broker with empty disk, you solve it.  The broker nodes have persistent data so they need to be synchronized first from the other brokers before the new broker can get productive. And at the same time, this would possibly mean some data loss and some other problems. So it is not as easy as deleting the old and starting a new one.

This is the part where networked storage is better because you can just mount it on another node and recover from the same disk

**Local storage** gives you usually a very good performance and doesn’t put load on the networking. But it also means that when the node dies, it is not so easy to recover the broker as you need to move the broker to a new node and possibly resync all the data from the other brokers which will take a long time and put temporary load on networking and your Kafka cluster. With networked storage on the other hand, you would just move the Kafka pod, mount the networked voume on anohter node and be ready to go very quickly without the need to resync all data. So it is not like the local storage does not offer availablity and reliability. But there are trade-offs and you need to decide if they are acceptable and worth it or not.

While the data are re-syncing after a complete node failure, your availability s of course weakened since the broker is not fully available.
The Kafka broker consists of a chain of 3 resources => Pod -> PVC -> PV
When the node goes offline, the PV is not available when you use Local Persistent Volumes
So the Pod and the PVC will nto be able to function without it
If the node gets back online again, the PV should be there again and the pod can start again
So this way it can handle a temporary outage of the node.
If the node dies completely - for example physically in a fire ... then the PV is essentially lost but the Pod will be waiting for it
So what you would need to do is basically go and **manually delete** the PVC and the PV
That frees the Pod from the old Pv which is lost. And the operator will create a new PVC which will bind to a new PV on some other node (assuming there is another node with another PV with corresponding size etc.)
And once you have the new PVC and PV, the Pod with the broker can start
It basically starts with an empty disk and will automatically connect to the remaining nodes of the Kafka cluster and resync the data from there (this of course assumes you have replicated topics and use them properly)
The manual interaction of deleting the PV and PVC is basically needed, because that is how you tell the Operator and Kubernetes that this node / PV is gone forever and it should not wait for it.
If the operator did it automatically, it would not be able to distinguish between some temporary outage (e.g. HW or SW upgrade) and ocmplete failure.

> _NOTE_ for each broker, all the drives should be on the same node

## Architecture

The broker does not track the consumer position (offset).
Compact: last value for the key is kept. (when value is null, the key is deleted)
Replicas provide redundancy => availability
  - Leader
  - Followers: they can be "in-sync" (ISR), since 2.4 followers can serve consumers. The latter option (not default) can save network bandwidth when the consumers are in one of the data center where the broker are deployed => data flow once from leader to follower and the consumer can get messages without generating extra traffic with the leader.
Acknowledge:
  - no ack
  - leader (what if the leader dies after the message is written without coping in the replica)
  - ISR
Consumer can handle the offset in custom way, e.g. in the database which works really nicely from transactional point of view. (QoS exactly once)
  - by default the client store the offset in a special topic `__consumer_offset`
  - consumer is part of a consumer group
  - one the consumer is the group lead (election and sync go through broker)
  - when new consumers join the group a partition re-balancing is required (WARNING: it's expensive operation so avoid to do it too often)
  - plug-gable strategy might be configured (e.g. to apply stickiness)
  - when there are more consumers in the group than partitions, the extra consumers will be idle
  - tool to set up the group

kafka-config.sh set up the quotas per client consumer

Kafka **Controller** is one of the broker:
  - maintain leader follower relationship
  - when a node shutdown, it tells other replicas to become leader
  - in rolling update, shut it down last
  - election through ZK

Security AMQ Streams additions:
  - Authentication OAuth -> RHSSO (but also other providers)
  - Authorization using keycloak authorization service (RHSSO only)


At topic level you can configure how many ISR, if the ack is ISR, if one of the ISR are not in sych, the topic become read only.

Client connects to the bootstrap node, gets metadata, then connect to the proper broker


Zookeeper 3 or 5: 5 is useful because if you have to perform maintenance activities on one node, you can still bear with one node failing (3 out of 5 is still a quorum)

When deployed in 2 data centers, one primary (3 nodes) and secondary (2 ZK nodes)

In OCP ZK is used internally by the cluster, no client have access to it

`advertised` is how the listeners can be accessed by the client (through the metadata)

## Good Practices for Designing Events

- Follow a standard naming pattern when naming events, including using past tense to indicate what happened in your business domain. For example, the name UserCreated states what happened to the user.

- Use narrow data types to avoid ambiguity in the meaning of the data. For example, if you have an integer field, always use an integer type instead of a string type.

- Create single-purpose events to avoid overloading an event type with different business logic's or meanings.

- Use one logical event definition per stream to avoid confusing what the event is and what the event stream represents. This means that no stream should contain two types of events.

## Client API compatibility 

Configurations that are available for their specific client
version should work just fine, unless they are using a very old client
version that we never supported.

The Kafka protocol is backwards and forwards compatible, however there
can be issues in case of message format changes. This hasn't happened
for a long time, but there is no guarantee.

If you want to go deeper, you can use the `kafka-broker-api-versions.sh`
tool against a specific Kafka version to see which is the latest
version supported for every protocol API.

## Streams API

- An event stream is a continuous flow of events without clearly defined limits, such as the number of events.

- Apache Kafka provides the Kafka Streams library to implement distributed data streaming applications.

- Each Kafka Streams pipeline defines a processing topology. A processing topology is a set of functions a streaming pipeline applies to each record.

- Kafka Streams enables you to structure a stream of events as a table, and a table as a stream of events. This is called the stream-table duality.

- Kafka Streams enables you to manipulate data streams by using the KTable, GlobalKTable, and KStreams objects.

### KTable

```java
        builder.table(
            "turbines",
            Consumed.with(Serdes.Integer(), turbineSerde),
            Materialized
                .<Integer, WindTurbine, KeyValueStore<Bytes, byte[]>>as("turbines-store")
                .withKeySerde(Serdes.Integer())
                .withValueSerde(turbineSerde)
        );  
```

## Tuning

Partitions:
- too many (1000) slow down performances
- too few (<10) clients cannot scale up

`rack` configuration allows a consumer to consume messages from the closest replica when a Kafka cluster spans multiple datacenters (!!!)


[JVM Config](https://access.redhat.com/labs/jvmconfig/)

## Articles

[Kafka for Cybersecurity (Part 3 of 6) - Cyber Threat Intelligence - Kai Waehner](https://www.kai-waehner.de/blog/2021/07/15/kafka-cybersecurity-siem-soar-part-3-of-6-cyber-threat-intelligence/)

[Process Apache Kafka records with Knative;s serverless architecture | Red Hat Developer](https://developers.redhat.com/articles/2022/03/14/process-apache-kafka-records-knatives-serverless-architecture#create_a_managed_kafka_instance_and_topic)

[Use Red Hat's SSO to manage Kafka broker authorization](https://developers.redhat.com/articles/2022/05/04/use-red-hats-sso-manage-kafka-broker-authorization)

[The Red Hat Cloud way: Event-driven, serverless, distributed cloud services to support modern apps](https://developers.redhat.com/articles/2022/05/03/red-hat-cloud-way-event-driven-serverless-distributed-cloud-services-support)


## Smart Events

[sandbox/DEMO.md at main · 5733d9e2be6485d52ffa08870cabdee0/sandbox · GitHub](https://github.com/5733d9e2be6485d52ffa08870cabdee0/sandbox/blob/main/DEMO.md)

[GitHub - 5733d9e2be6485d52ffa08870cabdee0/sandbox](https://github.com/5733d9e2be6485d52ffa08870cabdee0/sandbox)



## Ecosystem

[Red Hat OpenShift Data Science Workshop - Object Detection :: Object Detection Workshop](https://redhat-scholars.github.io/rhods-od-workshop/od-workshop/index.html)

[Enrich your Ceph Object Storage Data Lake by leveraging Kafka as the Data Source](https://itnext.io/enrich-your-ceph-object-storage-data-lake-by-leveraging-kafka-as-the-data-source-e9a4d305abcf)

## Positioning

[Apache Pulsar messaging](https://pulsar.apache.org/)

Hints and tips
-------------------------------------------------------------------------------

### Get the bootstrap endpoint

    oc get kafka my-cluster -o=jsonpath='{.status.listeners[].bootstrapServers}{"\n"}'

### Consuming messages

```
oc exec -it my-cluster-kafka-0 -- bin/kafka-console-consumer.sh \
        --bootstrap-server my-cluster-kafka-bootstrap:9092 \
        --topic my-topic --from-beginning
```

> **NOTE** the `--from-beginning` option that forces the consumption of all messages stored in the topic

Remote connection through SSL:

- Create truststore with the remote ca certificate:

  ```sh
  oc extract secret/my-cluster-cluster-ca-cert --keys=ca.crt --to=- > kafka-cluster.crt
  keytool -import -trustcacerts -alias root -file kafka-cluster.crt -keystore truststore.jks -storepass password -noprompt
  ```

- Launch command line consumer:

  ```sh
  ./kafka-console-consumer.sh \
    --bootstrap-server my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443 \
    --consumer-property security.protocol=SSL \
    --consumer-property ssl.truststore.password=password \
    --consumer-property ssl.truststore.location=truststore.jks \
    --topic my-topic --from-beginning
  ```

Remote connection through SASL_SSL:

- Create `config-sasl.properties`

  ```
  security.protocol=SASL_SSL
  ssl.truststore.location = cloud-truststore.jks
  ssl.truststore.password = password
  sasl.mechanism=PLAIN
  sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="" password="" ;
  ```

- Launch command line consumer:

  ```sh
  ./kafka-console-consumer.sh \
    --bootstrap-server dm-bus-cbppg-n-ocpgubcoc--g.bf2.kafka.rhcloud.com:443 \
    --consumer.config config-sasl.properties \
    --topic new-customers --from-beginning
  ```

Other options for sasl:

```
sasl.mechanism=SCRAM-SHA-256
sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="" password="" ;
```

- Launch a standalone consumer in its own pod
```sh
oc run kafka-consumer -ti \
  --image=registry.redhat.io/amq7/amq-streams-kafka-31-rhel8:2.2.0 \
  --rm=true --restart=Never \
  -- bin/kafka-console-consumer.sh \
  --bootstrap-server $KAFKA_CLUSTER_NAME-$KAFKA_CLUSTER_NS-bootstrap:9092 \
  --topic $KAFKA_TOPIC --from-beginning
```

### Producing messages

Within the OC cluster:

```sh
oc exec -it my-cluster-kafka-0 -- bin/kafka-console-producer.sh \
                                 --bootstrap-server my-cluster-kafka-bootstrap:9092 \
                                 --topic my-topic
```

Using local client (SSL):

  ```sh
  ./kafka-console-producer.sh \
    --bootstrap-server my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443 \
    --producer-property security.protocol=SSL \
    --producer-property ssl.truststore.password=password \
    --producer-property ssl.truststore.location=truststore.jks \
    --topic my-topic
   ```

Messages with keys:

  ```sh
  ./kafka-console-producer.sh \
    --bootstrap-server my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443 \
    --topic my-topic \
    --property parse.key=true \
    --property key.separator=,
   ```

Remote connection through SASL_SSL (requires property file defined before):

  ```sh
  ./kafka-console-producer.sh \
    --bootstrap-server dm-bus-cbppg-n-ocpgubcoc--g.bf2.kafka.rhcloud.com:443 \
    --producer.config config-sasl.properties \
    --topic new-customers
  ```

### Create topic

```sh
./kafka-topics.sh \
  --bootstrap-server my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443 \
  --create \
  --topic my-topic \
  --partitions 3 \
  --replication-factor 3
```


### describe topic

In OCP:

```sh
oc exec -it my-cluster-kafka-0 -- \
  bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --describe --topic event
```

Create config file:

```
bootstrap.server=my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443 
security.protocol=SSL 
ssl.truststore.password=password 
ssl.truststore.location=truststore.jks 
```

run the command:

```sh
./kafka-topics.sh \
  --bootstrap-server my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443 \
  --describe \
  --command-config config.properties \
  --topic my-topic 
```

### List topics

```sh
oc exec -c kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-topics.sh --list --bootstrap-server localhost:9092 
```
CLIENT-IDhost, log-end-offset, lag, consumer-id, host, client-id

```sh
oc exec -it my-cluster-kafka-0 -- bin/kafka-consumer-groups.sh \
        --bootstrap-server my-cluster-kafka-brokers:9092 \
        --describe --group total-connected-devices
```

The **consumer lag** is the difference between the last offset (current) in the partition and the latest offset **committed** by the consumer.

Pay attention: consumers often do NOT commit the message as the consume it. For performance reason, usually clients commit the message every 5 seconds (`auto.commit.interval.ms`).

```
mp.messaging.incoming.[channel].auto.commit.interval.ms=1000
```

### Partitions

How to show the partition size:

```
oc exec -it my-cluster-kafka-0 -- bin/kafka-log-dirs.sh --describe \
        --bootstrap-server my-cluster-kafka-bootstrap:9092 --topic-list event \
        | grep '^{' \
        | jq '.brokers[] | {broker: .broker, partitions: [ .logDirs[].partitions[] | {name: .partition , size: .size} ]}'
```

Show the partitions and the offsets

```
oc exec -it my-cluster-kafka-0 -- bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic event
```

### kcat 

Formerly known ad Kafka Cat.

SSL config file:

```
bootstrap.servers=event-broker-dr-kafka-tls-bootstrap-appdev-kafka.apps.ocp4.dmshift.eu:443
enable.ssl.certificate.verification=false
security.protocol=SSL 
```

Show all events on the topic:

```sh
kcat -F config.properties -t inventory.inventory.orders -C \
    -f '\nKey (%K bytes): %k
      Value (%S bytes): %s
      Timestamp: %T
      Partition: %p
      Offset: %o
      Headers: %h\n'
```

## Running admin script

In productin environment to avoid disturbing the broker pod with another workload, it's recommended to run the admin tasks in their own pod (`oc run`): 

```sh
oc run kafka-admin -ti --image=registry.redhat.io/amq7/amq-streams-kafka-31-rhel8:2.1.0 --rm=true --restart=Never -- \
  ./bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --topic topic-example --delete
```

Administration
-------------------------------------------------------------------------------

Broker config:

    oc exec -c kafka <broker-pod> -- cat custom-config/server.config

Show Broker dynamic config:

    oc exec -c kafka <broker-pod> -- bin/kafka-configs.sh --entity-type brokers --entity-name 0 --describe --bootstrap-server localhost:9092

Show topic dynamic config:

    oc exec -c kafka <broker-pod> -- bin/kafka-configs.sh --entity-type topics --entity-name <topic-name> --describe --bootstrap-server localhost:9092


Securing communication
-------------------------------------------------------------------------------

### Expose Kafka cluster with an SSL encrypted route

Edit kafka CRD to switch the tls listener in type `route`

```yaml
apiVersion: kafka.strimzi.io/v1beta2
kind: Kafka
metadata:
  name: my-cluster
  namespace: amq-streams-kafka
spec:
  kafka:
    # ...
    listeners:
      # ...
      - name: listener1
        port: 9093
        type: route
        tls: true
```

Get the route endpoint:

```yaml
oc get routes my-cluster-kafka-tls-bootstrap -o=jsonpath='{.status.ingress[0].host}{"\n"}'
```

### Client side security

Extract the cerficate and create the trust store:

```sh
oc extract secret/my-cluster-cluster-ca-cert --keys=ca.crt --to=- > kafka-cluster.crt
keytool -import -trustcacerts -alias root -file kafka-cluster.crt -keystore truststore.jks -storepass password -noprompt
```

Otherwise to extract the certificate from the endpoint:

```sh
echo | openssl s_client -servername <URL> -connect <URL>:443| \
sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > kafka-cluster.crt
```

For java client you can create a truststore and import the previous certificate

```sh
keytool -import -trustcacerts -alias root \
  -file kafka-cluster.crt -keystore truststore.jks \
  -storepass password -noprompt
```

JDK comes with its own key store where the common CA certificates are stored.

Usually, in Linux the key store is located in the following path: `/usr/lib/jvm/java/lib/security/cacerts`

The standard key store password is `changeit`.

In order to work with CA issued certificates and self signed ones, you can copy the default key store and add self signed one. Avoid to alter the default trust store.

To import the self signed certificate:

```sh
keytool -import -trustcacerts -alias dmshift -file kafka-cluster.crt -keystore truststore.jks -storepass changeit --noprompt
```

Useful reference to generate a full set of self signed certificate:

https://mariadb.com/docs/security/data-in-transit-encryption/create-self-signed-certificates-keys-openssl/
https://docs.confluent.io/2.0.1/kafka/ssl.html


Mutual authentication:

```sh
./kafka-console-producer.sh \
                               --bootstrap-server my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443 \
                               --producer-property security.protocol=SSL \
                               --producer-property ssl.truststore.password=password \
                               --producer-property ssl.truststore.location=/home/donato/git/appsvc-notes/pvt/ssl/truststore.jks \
                               --producer-property ssl.keystore.location=/home/donato/git/appsvc-notes/pvt/ssl/keystore.jks \
                           --producer-property ssl.keystore.password=password \
                               --topic my-topic
```

#### Scram config

```
kafka.ssl.truststore.location = ../truststore.jks
kafka.ssl.truststore.password = password

## SASL SCRAM-SHA-512
kafka.security.protocol=SASL_SSL
kafka.sasl.mechanism=SCRAM-SHA-512
kafka.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username="<user>" password="<passwd>";
```

#### Certificates naming conventions

The file extensions in a SSL context:

- `.crt` is the certificate produced by the certificate authority that verifies the authenticity of the key. (The key itself is not included.) This is given to other parties, e.g. HTTPS client.
- `.csr` is the certificate request. This is a request for a certificate authority to sign the key. (The key itself is not included.)
- `.key` contains the private key for the certificate.

High availability across racks
-------------------------------------------------------------------------------

Anti Affinity:

```yaml
          podAntiAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
              - labelSelector:
                  matchExpressions:
                    - key: strimzi.io/name
                      operator: In
                      values:
                        - my-cluster-zookeeper
                topologyKey: "kubernetes.io/hostname"
```

Spread Constraint:

```yaml
    template:
      pod:
        topologySpreadConstraints:
        - labelSelector:
            matchExpressions:
              - key: strimzi.io/name
                operator: In
                values:
                  - my-cluster-kafka
          topologyKey: "kubernetes.io/hostname"
          maxSkew: 1
          whenUnsatisfiable: DoNotSchedule #ScheduleAnyway
```

Rack awareness:

label the zone

```sh
oc label node master-0 topology.kubernetes.io/zone=dc1
```

```yaml
    rack:
      topologyKey: kubernetes.io/hostname
      #topologyKey: topology.kubernetes.io/zone
```


Troubleshooting
-------------------------------------------------------------------------------

Communication:

- Run with `-Djavax.net.debug=all`

Network:

- [How to begin Network performance debugging](https://access.redhat.com/articles/1311173)

When on the client side you get an error with an error id, e.g.:

```
2023-06-06 17:50:29,556 DEBUG Runtime failure during token validation (ErrId: 5cbf1e54)
```

You can search that id in the server log to gather further insights.

To raise the log verbosity use the following configuration:

```yaml
spec:
  kafka:
    logging:
      type: inline
      loggers:
        log4j.logger.io.strimzi: "DEBUG"
```

Check Kafka config
-------------------------------------------------------------------------------

```sh
oc exec -it my-cluster-kafka-0 -- bin/kafka-configs.sh \
   --bootstrap-server my-cluster-kafka-bootstrap:9092 \
   --describe --entity-type brokers --entity-name 1 --all
```


Users Management
-------------------------------------------------------------------------------

Set password

```
oc create secret generic kafkausers-secrets --from-literal=KUSER_PWD=Secret123
```

Show password

```sh
oc get secret kafkausers-secrets -o yaml
echo "U2VjcmV0MTIz" |base64 -d
```

Create users pointing to the previous secrets.

[Example of user definition](../camel-k/kuser.yaml)

### The state store, topic-store, may have migrated to another instance

Solution: delete the entity operator pod