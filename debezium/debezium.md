Debezium
==========================================================================

## TO READ

https://debezium.io/blog/2019/10/01/audit-logs-with-change-data-capture-and-stream-processing/

https://github.com/debezium/debezium-examples/tree/main/auditlog

https://github.com/debezium/debezium-examples/tree/main/outbox


## Example

```sh
oc new-project debezium
```

deploy kafka

deploy mysql:


```sh
oc new-app \
	-e MYSQL_USER=debezium \
	-e MYSQL_PASSWORD=dbzpwd \
	-e MYSQL_DATABASE=inventory \
	mysql
```
   
Enter in the container:
```
oc rsh 

# -------------------
mysql -u root

mysql> GRANT ALL PRIVILEGES ON *.* TO 'debezium'@'%'
Query OK, 0 rows affected (0.06 sec)

mysql> flush privileges;
Query OK, 0 rows affected (0.02 sec)
```

Deploy #kafka_connect 

```sh
oc apply -f kafka-connect.yaml

oc describe kafkaconnect

oc exec -c kafka my-cluster-kafka-0 -- /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning --max-messages 1
```

errors:

```
2022-05-26 16:23:09,271 INFO [debezium-connector|task-0] 	GRANT RELOAD ON *.* TO `debezium`@`%` (io.debezium.connector.mysql.SnapshotReader) [debezium-mysqlconnector-inventory-snapshot]
2022-05-26 16:23:09,271 INFO [debezium-connector|task-0] 	GRANT ALL PRIVILEGES ON `inventory`.* TO `debezium`@`%` (io.debezium.connector.mysql.SnapshotReader) [debezium-mysqlconnector-inventory-snapshot]
```

```
ERROR [debezium-connector|task-0] Failed due to error: Aborting snapshot due to error when last running 'UNLOCK TABLES': Access denied; you need (at least one of) the SUPER, REPLICATION CLIENT privilege(s) for this operation (io.debezium.connector.mysql.SnapshotReader) [debezium-mysqlconnector-inventory-snapshot]
org.apache.kafka.connect.errors.ConnectException: Access denied; you need (at least one of) the SUPER, REPLICATION CLIENT privilege(s) for this operation Error code: 1227; SQLSTATE: 42000.
	at io.debezium.connector.mysql.AbstractReader.wrap(AbstractReader.java:241)
```