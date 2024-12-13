# Camel Example

https://camel.apache.org/camel-quarkus/2.14.x/user-guide/first-steps.html


## Route class


```java
import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class Routes extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // from...
    }
}
```

## Extract JSON field

dependencies

```xml
    <dependency>
      <groupId>org.apache.camel.quarkus</groupId>
      <artifactId>camel-quarkus-jackson</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel.quarkus</groupId>
      <artifactId>camel-quarkus-bean</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel.quarkus</groupId>
      <artifactId>camel-quarkus-timer</artifactId>
    </dependency>
    <dependency>
      <groupId>org.apache.camel.quarkus</groupId>
      <artifactId>camel-quarkus-log</artifactId>
    </dependency>
```

```java
from("timer:java?period=5000").id("route-java")
    .setBody(simple("{\"FirstName\":\"Fred\",\"Surname\":\"Smith\",\"Age\":28,\"Address\":{\"Street\":\"Brick Lane\",\"City\":\"London\",\"Postcode\":\"E1 6RF\"}}"))
    .unmarshal()
        .json()
    .transform().simple("${body['Address']['Street']}")
    .to("direct:log_body");
```