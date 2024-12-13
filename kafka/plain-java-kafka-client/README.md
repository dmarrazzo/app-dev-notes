# Plain Java Kafka Client

## Find and update the bootstrap URL

```sh
oc get routes my-cluster-kafka-tls-bootstrap -o=jsonpath='{.status.ingress[0].host}{"\n"}'
```

Use the result of the previous command to update the property:

```java
    props.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "<URL>:443");
```

## Secure communication

Extract the cerficate and create the trust store:

```sh
oc extract secret/my-cluster-cluster-ca-cert --keys=ca.crt --to=- > kafka-cluster.crt
keytool -import -trustcacerts -alias root -file kafka-cluster.crt -keystore truststore.jks -storepass password -noprompt
```

Make sure the in the code you have the right path to `truststore.jks`

```java
    props.put( SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "<path>/truststore.jks");
```

If you are using Red Hat OpenShift Streams for Apache Kafka, it is possible to retrieve the certificate in this way:

```sh
echo | openssl s_client -servername <URL> -connect <URL>:443| \
sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > kafka-cluster.crt
```

To set the plain authentication:

```java
props.put(SaslConfigs.SASL_MECHANISM,"PLAIN");
props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=<username> password=<password> ;");
```

## Build

```sh
mvn clean package
```

## Run the producer
```sh
cd producer
mvn exec:java
```

## Run the consumer

```sh
cd consumer
java -jar target/consumer-1.0.jar
```