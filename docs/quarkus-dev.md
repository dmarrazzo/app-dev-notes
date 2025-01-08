Microservices Architecture Patterns
===================================================================================================================================

Comparing Synchronous and Asynchronous Interprocess Communication
-----------------------------------------------------------------------------------------------------------------------------------

### Advantages

- **Decouples** the client from the service: The client is unaware of service instances. No discovery mechanism required.

- Implements **message buffering**: The message broker queues messages in a message buffer when the consumer is slow or unavailable.

- Enables **flexible** client-service interaction: The communication between client and service is flexible. There is no requirement for clients to be available to receive the messages. Messaging supports various styles to ensure message delivery.

### Disadvantages

- Increases the operational **complexity**: There is additional configuration for message components. The message broker component must be highly available to ensure system reliability.

- Increases the implementation complexity of request-and-response based interactions: Each request message must contain a reply channel and a _correlation_ identifier. The service writes the response and the correlation identifier to the reply channel. The client identifies the message by using that correlation identifier.

### Designing for Failure

- Time-out
- Retry
- Fallback
- Circuit Breaker
- Bulkhead (or Connection Pools)

### Designing for Observability

**Distributed tracing** is a method of tracking the requests made by your application to different services to accomplish a task. This method provides complete information of the application's behavior as the requests pass through multiple services.

- The service that creates the initial request, assigns to it a unique ID, or **trace ID**.

- The services involved in handling the request, pass the original _trace ID_ to the subsequent requests.

- Every service generates a data structure called **span** and is identified with a _unique ID_.

  - Span: start and stop timestamps, and business-relevant data.

- Every service adds the span ID to the trace.

A **central aggregator** collects all the span data for storage and visualization purposes.

### Designing for Security

- Single Sign-On

- Distributed sessions: distributing identity among microservices

- Client-side token: a microservice validates the token without calling the authentication service. JSON Web Token (JWT)

- Client-side token with API Gateway: API gateways cache client-side tokens. The validation of tokens is handled by the API gateway.

https://opentelemetry.io/
https://prometheus.io/
https://grafana.com/

MicroProfile
-----------------------------------------------------------------------------------------------------------------------------------

The MicroProfile community has defined 12 specifications so far.

MicroProfile **platform** specifications

- Config	Externalizes application configuration
- Fault Tolerance	Defines multiple strategies to improve application robustness
- Health	Expresses application health to the underlying platform
- JWT RBAC	Secures RESTful endpoints
- Metrics	Exposes platform and application metrics
- Open API	Java APIs for the OpenAPI specification that documents RESTful endpoints
- OpenTelemetry	Defines behaviors and an API for accessing an OpenTelemetry-compliant Tracer object
- REST Client	Type-safe invocation of REST endpoints

MicroProfile **standalone** specifications

- GraphQL	Java API for the GraphQL query language
- Reactive Streams operators	Provides asynchronous streaming to be able to stream data
- Reactive Streams messaging	Provides asynchronous messaging support based on Reactive Streams

Quarkus MicroProfile Implementations:

| MicroProfile Specification |  Project Name                         |  Quarkus Dependency (Maven group:artifact)
|----------------------------|---------------------------------------|------------------------------------------------
| Rest Client                |  SmallRye REST Client                 |  io.quarkus:quarkus-smallrye-rest-client
| Fault Tolerance            |  SmallRye Fault Tolerance             |  io.quarkus:quarkus-smallrye-fault-tolerance
| Health Check               |  SmallRye Health                      |  io.quarkus:quarkus-smallrye-health
| Metrics                    |  SmallRye Metrics                     |  io.quarkus:quarkus-smallrye-metrics
| JWT Security               |  SmallRye JWT                         |  io.quarkus:quarkus-smallrye-jwt
| OpenAPI                    |  SmallRye OpenAPI                     |  io.quarkus:quarkus-smallrye-openapi
| OpenTelemetry              |  Quarkus OpenTelemetry                |  io.quarkus:quarkus-opentelemetry
| Reactive Streams Operators |  SmallRye Reactive Streams Operators  |  io.quarkus:quarkus-smallrye-reactive-streams-operators
| Reactive Streams Messaging |  SmallRye Reactive Messaging          |  io.quarkus:quarkus-smallrye-reactive-messaging

Quarkus
===================================================================================================================================

Project Creation
-----------------------------------------------------------------------------------------------------------------------------------

```
mvn com.redhat.quarkus.platform:quarkus-maven-plugin:2.7.6.Final-redhat-00009:create \
    -DplatformVersion=2.7.6.Final-redhat-00009 \
    -DprojectGroupId=com.redhat.training \
    -DprojectArtifactId=getting-started \ 
    -Dextensions="resteasy"
```

https://code.quarkus.redhat.com/

### Add extensions

```sh
./mvnw quarkus:add-extension -Dextensions='<extension-name>'
```

### Config Specification

Priorities:

1. Java system properties
2. Environment variables
3. The environment file
4. K8s secrets
5. K8s config maps
6. Application properties

```
api.endpoint.host = api.example.com
endpoint.url = https://${api.endpoint.host}/
```

```java
@ConfigProperty(name = "debug-flag", defaultValue = "false")
public boolean debugFlag;

@ConfigProperty(name = "unit")
String unit;
```

Group of config:

```java
@ConfigMapping(prefix = "example")
public interface ExampleConfiguration {
    Optional<String> format();

    @WithDefault("false")
    boolean debugFlag();

    String unit();
}

(...)

@Inject
ExampleConfiguration config;
```

Property converter, e.g.: `example.user=student:student@redhat.com`

Implementing `Converter`:

`public class ExampleUserConverter implements Converter<ExampleConfiguration.ExampleUser> {...}`

Properties by env:

```
example.user.username = student
%dev.example.user.username = teststudent
%test.example.user.username = student@testing.com
%dev.example.user.username = devstudent
%stage.example.user.username = stagestudent
```

`stage` is a custom profile.

Override profile:

```
java -jar -Dquarkus.profile=dev target/quarkus-app/quarkus-run.jar
```

### Logging

Use JBoss Logging for application logging:

```java
import io.quarkus.logging.Log;

// in the method:
Log.info("Simple!"); 
```

application.properties:
```
quarkus.log.level=INFO
quarkus.log.category."org.hibernate".level=DEBUG
```

Developing REST
-----------------------------------------------------------------------------------------------------------------------------------

Extensions:

- `quarkus-resteasy`
- `quarkus-resteasy-jackson`

Expose class as REST resource:

```java
@Path("/expenses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExpenseResource {

    @GET
    public Set<Expense> list() {...}

    @DELETE
    @Path("{uuid}")
    public Set<Expense> delete(@PathParam("uuid") UUID uuid) {...}
```

> **WARNING:** Pay attention to avoid circular Json references `@JsonIgnore`

### Consume REST services

Extension: `quarkus-smallrye-rest-client`

Register the client:

```java
@Path("/expenses")
@RegisterRestClient
public interface ExpenseServiceClient {...}
```

Inject the client (Inject is optional?):

```java
@Inject
@RestClient
ExpenseServiceClient expenseServiceClient;
```

Configure the service URL:

```
quarkus.rest-client."com.redhat.training.client.ExpenseServiceClient".url=http://localhost:8080
```

Override via env var:

```sh
QUARKUS_REST_CLIENT__COM_REDHAT_TRAINING_CLIENT_EXPENSESERVICECLIENT__URL="http://expense-service:8080"
```

The standard MicroProfile Rest Client properties notation can also be used to configure the client:

```
<Fully Qualified REST Client Interface>/mp-rest/url=<Remote REST base URL>
<Fully Qualified REST Client Interface>/mp-rest/scope=javax.inject.Singleton
```

To facilitate the configuration, you can use the config key:

```java
@RegisterRestClient(configKey="expense-api")
```

application.properties:

```
expense-api-rest-client/mp-rest/url="http://expense-service:8080"
```

Override via env var:

```
EXPENSE_API_REST_CLIENT_MP_REST_URL="http://expense-service:8080"
```

 > **WARNING** it's only possible to overwrite namespaced properties that already exist in `application.properties` via environment variables but not to introduce them.

Handling POST method:

```java
@Transactional
@POST
public Response create(Person person) {
    person.persist();
    // Return as response header 'Location: http://localhost:8080/persons/{id}'
    return Response.created(URI.create("/persons/" + person.id)).build();
    // OTHERWISE Response whole serialized entity
    return Response.status(Status.CREATED).entity(person).build();
}
```

### Enable Open API

Extension: `quarkus-smallrye-openapi`

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-openapi</artifactId>
</dependency>
```

Get the api `/q/openapi`

auto-generate this Operation Id:

```
mp.openapi.extensions.smallrye.operationIdStrategy=METHOD
```

Data Persistence with Panache
-----------------------------------------------------------------------------------------------------------------------------------

Panache and JDBC extensions:

- `quarkus-hibernate-orm-panache`
- `quarkus-jdbc-postgresql`

Properties (with log enabled):

```
quarkus.hibernate-orm.database.generation=drop-and-create
quarkus.hibernate-orm.log.sql=true
quarkus.hibernate-orm.sql-load-script=import.sql
```

Dev service port: `quarkus.datasource.devservices.port=14000`

Panache entity: 

```java
@Entity
public class Person extends PanacheEntity {

    public String name;
    public LocalDate birth;
    public Status status;
```

Populate the DB with `src/main/resources/import.sql`

```sql
insert into person (id, name, birth, status) values (nextval('hibernate_sequence'), 'donato',  '1973-04-04 00:00:00.000', 1);
```

Most useful operations:

```java
// creating a person
Person person = new Person();
person.setName("Stef");
person.setBirth(LocalDate.of(1910, Month.FEBRUARY, 1));
person.setStatus(Status.Alive);

// persist it
person.persist(person);
```

### Active Record Pattern

```java
Person.listAll();

Person.findById(id);

Person.list("status", Status.valueOf(status.toUpperCase()));

Person.deleteById(id);

Person.update("name = ?2 where id = ?1", id, name);

Person.count();
```

Only use `list` and `stream` methods if your table contains small enough data sets. 

For larger data sets you can use the find method equivalents, which return a PanacheQuery on which you can do paging:

```java
// create a query for all living persons
PanacheQuery<Person> livingPersons = Person.find("status", Status.Alive);

// make it use pages of 25 entries at a time
livingPersons.page(Page.ofSize(25));

// get the first page
List<Person> firstPage = livingPersons.list();

// get the second page
List<Person> secondPage = livingPersons.nextPage().list();

// get page 7
List<Person> page7 = livingPersons.page(Page.of(7, 25)).list();

// get the number of pages
int numberOfPages = livingPersons.pageCount();

// get the total number of entities returned by this query without paging
long count = livingPersons.count();

// and you can chain methods of course
return Person.find("status", Status.Alive)
    .page(Page.ofSize(25))
    .nextPage()
    .stream()
```

### Repository pattern

Put your custom query update logic in a repository class:

```java
@ApplicationScoped
public class PersonRepository implements PanacheRepository<Person> {

   public Person findByName(String name){
       return find("name", name).firstResult();
   }

   public List<Person> findAlive(){
       return list("status", Status.Alive);
   }

   public void deleteStefs(){
       delete("name", "Stef");
   }
}

///////////////////////
// Then use it:
@Inject
PersonRepository personRepository;
```

Further readings:

https://quarkus.io/guides/hibernate-orm-panache

### Relationship

One to many (mapped by if you need the reverse relationship)

```java
    @OneToMany(mappedBy = "menu", orphanRemoval = false)
    public List<MenuItem> menuItems;
```

Many to one: reverse relationship

```java
    @ManyToOne
    @JoinColumn(name = "menu_fk", nullable = true)
    public Menu menu;
```

### Join semantic

**Left outer join** Fetch all Persons regardless they have a address.

(_outer_ can be skipped bacause it is the default)

```java
Person.list("from Person p left outer join p.addresses");
Person.list("from Person p left join p.addresses");
```

Return: list of list [[Person1, Address1, ...],[Person2, Address2, ...]]

**Join** Fetch all Persons which have a address.

(_inner_ is implicit)

`from Person p join p.addresses where p.name is not ?1`

Return: list of list [[Person1, Address1, ...],[Person2, Address2, ...]]

**Fetch** this clause does not affect the relational query but limit the return to Person.

`from Person p join fetch p.addresses where p.name is not ?1`

Return: list of Person

### Parameters

```java
// Positional parameters
cheapBooks = Book.list("unitCost between ?1 and ?2", min, max);
// Named parameters
Map<String, Object> params = Map.of("min", min, "max", max);
cheapBooks = Book.list("unitCost between :min and :max", params);
// Using the Parameters class
cheapBooks = Book.list("unitCost between :min and :max",
Parameters.with("min", min).and("max", max));
// Passing an enumeration
List<Book> englishBooks = Book.list("language", Language.ENGLISH);
```

Dev Services
-----------------------------------------------------------------------------------------------------------------------------------

Disable dev services in `application.properties`:

```
quarkus.devservices.enabled=false
```

Start the kafka container:

```sh
podman run --rm -it -p 9092:9092 -e kafka.bootstrap.servers=OUTSIDE://localhost:9092 docker.io/vectorized/redpanda
```

PostgreSQL

```sh
podman run --rm -it -p 5432:5432 -e POSTGRES_USER=quarkus -e POSTGRES_PASSWORD=quarkus -e POSTGRES_DB=quarkus docker.io/library/postgres:14
```

Run sql command in a testcontainer:

```sh
$ podman exec -it <container-name> /bin/sh
# psql -U quarkus
```

Container Images
-----------------------------------------------------------------------------------------------------------------------------------

Extensions for building (and pushing) container images. 

### JIB

Jib is a library and Maven/Gradle plug-in, which creates reproducible builds of container images without any external tool such as Docker. 

```sh
./mvnw quarkus:add-extension -Dextensions='container-image-jib'
```

### Docker

To build container images using Docker 

```sh
mvn quarkus:add-extension -Dextensions="container-image-docker"
```

### Source2Image

This extension depends on the `BuildConfig` and `ImageStream` resources

```sh
mvn quarkus:add-extension -Dextensions="container-image-s2i"
```

### Building Container Images

Build (native is optional)

```
mvn package -Pnative -Dquarkus.container-image.build=true
```

To push your container image at the same time it is built:

```
mvn package -Pnative -Dquarkus.container-image.push=true
```

Properties:

```
quarkus.container-image.build=true
quarkus.container-image.group=quay.io
quarkus.container-image.name=expense-service
```

### Deploy OpenShift

Deploy (native) in OpenShift:

```
mvn package -Pnative -Dquarkus.kubernetes.deploy=true
```

OpenShift S2I quarkus native:

```
oc new-app https://<url-to-source-code>#<branch-name> \
  -i quay.io/quarkus/ubi-quarkus-native-s2i:20.2.0-java11
```

Change build container limits:

```
oc cancel-build bc/native-expenses
oc patch bc/native-expenses \
 -p '{"spec":{"resources":{"limits":{"cpu":"4", "memory":"3.5Gi"}}}}'
oc start-build native-expenses
```

Deploying Quarkus Applications in Red Hat OpenShift Container Platform
-----------------------------------------------------------------------------------------------------------------------------------

Extension:

- `quarkus-openshift`

Deploy: 

```sh
mvn package -Dquarkus.kubernetes.deploy=true
```

The Quarkus OpenShift extension generates four resource files in the `target/kubernetes` directory: `kubernetes.yml` and `openshift.yml`

Create route:

```
quarkus.openshift.expose=true
```

Other properties:

```
quarkus.openshift.name=my-cloudnative-app
quarkus.openshift.labels.release-status=production
quarkus.openshift.env-vars.DB_USERNAME.value=dbuser
quarkus.s2i.base-jvm-image=registry.access.redhat.com/ubi8/openjdk-11
```

Control how the container image is generated:

```
quarkus.container-image.group=com.myorganization
quarkus.container-image.name=quarkus-app
quarkus.container-image.tag=1.0
```

Building Container Images:

local podman / docker:

```sh
mvn clean package -Dquarkus.container-image.build=true
```

external registry:

```
quarkus.container-image.push=<valid registry>
```

The extension `quarkus-container-image-s2i` is using S2I binary builds in order to perform container builds inside the OpenShift cluster

[Quarkus container image](https://quarkus.io/guides/container-image)

```sh
mvn clean package \
  -Dquarkus.kubernetes.deploy=true \
  -Dquarkus.openshift.expose=true \
  -Dquarkus.s2i.base-jvm-image=registry.access.redhat.com/ubi8/openjdk-11 \
  -DskipTests
```

Fault Tolerance
-----------------------------------------------------------------------------------------------------------------------------------

Extension: `smallrye-fault-tolerance`

https://quarkus.io/guides/smallrye-fault-tolerance

### Timeout

`@org.eclipse.microprofile.faulttolerance.Timeout`
Defines the maximum execution time before interrupting a request.

```java
@Timeout(value=200, unit = ChronoUnit.SECONDS )
public Product getProduct(int id) {...}
```

- `value` the maximum amount of time the method might take before timing out

### Retry policy

`@org.eclipse.microprofile.faulttolerance.Retry`
Defines the conditions for retrying a failing execution.

```java
@Retry(maxRetries=90, maxDuration=100, retryOn={RuntimeException.class})
public Product getProduct(int id) {...}
```

- `maxRetries` The maximum number of retries.
- `maxDuration` The maximum amount of time that the retry attempts may take.
- `retryOn` The exception families that cause execution of a retry.
- `delay` The amount of time for the delays between each retry.
- `jitter` The range limit for a random amount of time added or subtracted to the delay.

### Fallback

`@org.eclipse.microprofile.faulttolerance.Fallback`
Executes an alternative method if the execution fails for the annotated method (after retry).

```java
@Fallback(fallbackMethod="getCachedProduct")
public Product getProduct(int id) { ... }
...

public Product getCachedProduct(int id) {
```


### Circuit breaker

`@org.eclipse.microprofile.faulttolerance.CircuitBreaker`
Supports a fail-fast approach, if the system is suffering from an overload or is unavailable.

Request window is a set of consecutive requests. If the number of failing requests inside this window is below a threshold, then the framework considers the service malfunctioning and opens the circuit.

```java
@CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 1000)
public List<String> getProducts() {...}
```
In the previous code, the circuit opens if half of the requests (`failureRatio = 0.5`) of four consecutive invocations (`requestVolumeThreshold=4`) fail. The circuit stays open for `1000` milliseconds and then it becomes half-open. After a successful invocation, the circuit is closed again.

- `requestVolumeThreshold` The number of consecutive invocations used as the baseline to calculate the number of failures.
- `failureRatio` The minimum failure ratio required to open the circuit.
- `delay` The amount of time the circuit stays open to review that the service is back to normal.

### Bulkhead

`@org.eclipse.microprofile.faulttolerance.Bulkhead`
Limits the workload to a microservice to avoid failures caused by concurrency or service overload.

```java
@Bulkhead(value = 5, waitingTaskQueue = 8)
public List getProducts() {...}
```

- `value` The number of requests that can be processed by the method.
- `waitingTaskQueue` The maximum number of requests in the queue.

### Policies order

- If the **Circuit** is open, then the Circuit Breaker policy throws an exception.
- If the **Bulkhead** and its queue are full, then it throws an exception.
- If none of the previous steps throw an exception, then the framework invokes the original endpoint method. ✅
- The **Timeout** policy interrupts the request if it takes too long.
- The **Retry** policy retries the request.
- The **Fallback** method returns the short-cut response if any of the previous throw an exception.

You can configure all annotations previously presented using the `application.properties` file and the following naming convention: `<classname>/<methodname>/<annotation>/<parameter>`

### Health Check

Extension: `quarkus-smallrye-health`

Without extra code, it checks the database connectivity

Exposed endpoints:

- `/q/health/live` - The application is up and running.
- `/q/health/ready` - The application is ready to serve requests.
- `/q/health/started` - The application is started.
- `/q/health` - Accumulating all health check procedures in the application.

**Liveness Probe**
Liveness probe checks the container health as we tell it do, and if for some reason the liveness probe fails, it restarts the container.
In fact, it could happen that the _Pod is running_ our application inside a container, but due to some reason let’s say _memory leak_, _cpu usage_, _application deadlock_ etc the application is not responding to our requests, and **stuck in error state**.

```java
@Liveness
public class MyLivenessCheck implements HealthCheck {

  public HealthCheckResponse call() {
    return HealthCheckResponse.up( "my-health-check" );
  }
}
```

**Readiness Probe**: the application is alive, but it cannot serve traffic unless some conditions are met e.g, populating a dataset, waiting for some other service to be alive etc. In such cases we use readiness probe. If the condition inside readiness probe passes, only then our application can serve traffic.

```java
@Readiness
public class MyReadinessCheck implements HealthCheck {

  @Override
  public HealthCheckResponse call() {
    return HealthCheckResponse.up( "my-health-check" );
  }
}
```
**Customize timeout**:

```
quarkus.openshift.liveness-probe.initial-delay=20s
quarkus.openshift.liveness-probe.period=45s
quarkus.openshift.readiness-probe.initial-delay=2s
quarkus.openshift.readiness-probe.period=15s
```

Native Quarkus Applications
-----------------------------------------------------------------------------------------------------------------------------------

```java
 @RegisterForReflection
    public static class Data {...}
```

Avoiding Private Access Modifiers

Quarkus' dependency injection implementation has limitations:

- If the member is not registered for reflection, then the reflective operation results in an unsupported operation
- This applies to injection fields, constructors, initializers, observer methods, producer methods and fields, disposers and interceptor methods.

### Third-party Libraries

create a `src/main/resources/reflection-config.json` file:

```json
[
  {
    "name" : "com.redhat.library.SpecialClass",
    "allDeclaredConstructors" : true,
    "allPublicConstructors" : true,
    "allDeclaredMethods" : true,
    "allPublicMethods" : true,
    "allDeclaredFields" : true,
    "allPublicFields" : true
  }
]
```

prop activate:

```
quarkus.native.additional-build-args =-H:ReflectionConfigurationFiles=reflection-config.json
```

Build:

```
mvn package -Pnative

(or)

mvn package -Pnative \
 -Dquarkus.native.container-build=true \
 -Dquarkus.native.container-runtime=podman \
 -Dquarkus.native.builder-image=registry.access.redhat.com/quarkus/mandrel-20-rhel8
```

image build:

```
podman build -f src/main/docker/Dockerfile.native .
```

https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus/1.7/html-single/compiling_your_quarkus_applications_to_native_executables/index

Quarkus Unit test
-----------------------------------------------------------------------------------------------------------------------------------

- `quarkus-junit5` extension
- Override the Maven Surefire Plugin version, log manager and Maven home folder.
- Test class and annotation `io.quarkus.test.junit.QuarkusTest`
- Annotate each test method with `org.junit.jupiter.api.Test`

Properties for tests:

```
quarkus.datasource.db-kind=postgresql
%test.quarkus.datasource.db-kind=h2
%dev.quarkus.hibernate-orm.sql-load-script = import-dev.sql
%test.quarkus.hibernate-orm.sql-load-script = import-test.sql
```

Common properties:

- `quarkus.http.test-port` The HTTP port the application will use when running in test mode. (8081)
- `quarkus.http.test-ssl-port` The HTTPS port the application will use when running in test mode. (8444)
- `quarkus.test.native-image-wait-time` Wait time for the native image to build during testing (5 min)
- `quarkus.test.native-image-profile` The profile to use when testing the native image (prod)
- `quarkus.test.profile` The profile to use when testing using @QuarkusTest (test)
- `quarkus.swagger-ui.always-include` Enable Swagger UI when the prod profile is not enabled. (false)
- `quarkus.http.test-timeout` When using REST-assured, time-out for obtaining a connection and a response. (30 sec)

### Test CDI

```java
@QuarkusTest
public class MyTestClass {

    @Inject
    MyService myService;

    @Test
    public void testMyService() {
        Assertions.assertTrue(myService.isWorking());
    }
}
```

```java
@TestHTTPEndpoint(MyService.class)
@TestHTTPResource
URL endpoint;
```

### REST-assured

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```

```java
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
...

    given()
      .when().get(endpoint)
      .then().body(is("hello"));
```

More concise, when one test per service:

```java
@QuarkusTest
@TestHTTPEndpoint(MyService.class)
public class MyTestClass {

  @Test
  public void testMyServiceEndPoint() {
    given()
      .when().get() // no endpoint here!
      .then().body(is("hello"));
  }
}
```

### Test resources

```java
@QuarkusTest
@QuarkusTestResource(DerbyDatabaseTestResource.class)
public class MyServiceTest {...}
```

Others:

- `H2DatabaseTestResource` Starts an in-memory H2 database.
- `LdapServerTestResource` Starts a local LDAP server.
- `KubernetesMockServerTestResource` Starts a Kubernetes API mock server.

### Native test

```java
@NativeImageTest
public class NativeExpensesCreationIT extends ExpensesCreationTest {...}
```
Build and then: `mvn test -Pnative`

### Quarkus DI Mock up

```java
@Mock
@ApplicationScoped
public class DataStoreMock extends DataStore {
  @Override
  public Data retrieve(UUID uuid) {
    // mocking logic
  }
}
```

### Mockito

Extension: `quarkus-junit5-mockito`

```java
DataStore dataStore = Mockito.mock(DataStore.class);
Mockito.when(dataStore.retrieve(uuid))
  .thenReturn(new Data(uuid, "name", 123));
Mockito.when(dataStore.retrieve(null))
  .thenThrow(new NullPointerException());
```

Annotation:

```java
@InjectMock
DataStore dataStore;

@BeforeEach
public void setup() {
  Mockito.when(dataStore.retrieve(null)).thenThrow(new NullPointerException());
}
```

The `Mockito.verify()` method is used at the end of a test. The method receives a mocked object and returns an object that shares the interface of the mock, the difference being that the logic it will execute is dependant on whether a method was called, and if specified, the number of times it was called. 

```java
DataStore dataStore = mock(DataStore.class);
when(dataStore.get(0)).thenReturn(new Data("name", 123));
System.out.println(dataStore.get(0).name);
verify(dataStore).get(0);
verify(dataStore, times(1)).get(0);
System.out.println(dataStore.get(0).age);
verify(dataStore, times(2)).get(0);
verify(dataStore, atMost(2)).get(0);
verify(dataStore, never()).get(1);
```

`io.quarkus.panache.mock.PanacheMock` remains the simplest way of mocking Panache classes and its static methods.

```java
PanacheMock.mock(Data.class);
Mockito.when(Data.find("name", "Joseph"))
  .thenReturn(new Data(UUID.randomUUID(), "Joseph", 123));
Data data = Data.find("name", "Joseph");
Assertions.assertEquals("Joseph", data.name);
```

Security
-----------------------------------------------------------------------------------------------------------------------------------


### CORS

```
quarkus.http.cors=true
quarkus.http.cors.origins=http://example.com,http://www.example.io
quarkus.http.cors.methods=GET,PUT,POST
quarkus.http.cors.headers=X-My-Custom-Header,X-Another-Header
```

### TLS

```
keytool -noprompt -genkeypair -keyalg RSA -keysize 2048 \
  -validity 365 \
  -dname "CN=myapp,OU=myorgunit, O=myorg, L=myloc, ST=state, C=country" \
  -ext "SAN:c=DNS:myserver,IP:127.0.0.1" \
  -alias myapp -storetype JKS \
  -storepass mypass -keypass mypass \
  -keystore myapp.keystore
```

```
quarkus.http.ssl.certificate.key-store-file=/path/to/myapp.keystore
quarkus.http.ssl.certificate.key-store-password=mypass
quarkus.http.ssl-port=8443
quarkus.http.ssl.certificate.key-store-file-type=[one of JKS, JCEKS, P12, PKCS12, PFX]
```

If the type is not provided, Quarkus will try to deduce it from the file extensions, defaulting to type JKS.

For Quarkus native builds, to enable TLS you must set the `quarkus.ssl.native` property to `true`

`quarkus.http.insecure-requests` property

- `enabled` The default option, both HTTP (8080) and HTTPS (8443) ports are open.
- `redirect` Plain HTTP requests will be automatically redirected to the HTTPS port.
- `disabled` The HTTP port will not be opened. Use this value for applications that should always be called securely using the HTTPS protocol.

### Mutual TLS (mTLS)

1. Extract the **public certificate** from the key store of a client application by using the following command:

   ```
   keytool -exportcert -keystore myclient.keystore \
     -storepass mypass -alias myclient \
     -rfc -file myclient.crt
   ```

2. Create a trust store. Import the **public key certificates** of all the applications that want to communicate with your application. 

   ```
   keytool -noprompt -keystore myapp.truststore -storepass mypass \
     -importcert -file myapp.crt \
     -alias myclient
   ```

3. Use the command in the previous step to import the certificate of the server on the client side.

4. Configure the client and server for mutual authentication. Start by adding the following properties to the server:

   ```
   quarkus.http.ssl.client-auth=required
   quarkus.http.ssl.certificate.trust-store-file=/path/to/myapp.truststore
   quarkus.http.ssl.certificate.trust-store-password=mypass
   ```

5. On the client side, you must add extra */mp-rest properties in the application.properties to securely communicate with the server:

   ```
   org.acme.client.mtls.GreetingService/mp-rest/url=https://myserver:8443
   org.acme.client.mtls.GreetingService/mp-rest/trustStore=/path/to/myclient.truststore
   org.acme.client.mtls.GreetingService/mp-rest/trustStorePassword=mypass
   org.acme.client.mtls.GreetingService/mp-rest/keyStore=/path/myclient.keystore 
   org.acme.client.mtls.GreetingService/mp-rest/keyStorePassword=mypass
   ```

### Authentication

Extensions:

- `quarkus-elytron-security-jdbc` validates identities performing JDBC queries from a database.
- `quarkus-security-jpa` uses JPA to validate identities from a data source.
- `quarkus-elytron-security-ldap` uses an LDAP server to validate the identity and retrieve user roles and groups.
- `quarkus-elytron-security-properties-file` embeds static identities in the application configuration file.

built-in authentication mechanisms: 
- `basic authentication`, which asks the request initiator for credentials and validates them against an Identity Provider - `quarkus.http.auth.basic`
- `form based authentication`, where credentials must be provided as form parameters - `quarkus.http.auth.form.enabled`

extensions implementing authentication mechanisms:

- `quarkus-smallrye-jwt` extracts the identity from a bearer JWT token
- `quarkus-oidc` enables the OpenID Connect authentication workflow. It supports Bearer Token for service applications and Authorization Token flow for web applications. This extensions relies on the `quarkus-smallrye-jwt` extension to handle identity tokens.
- `quarkus-elytron-security-oauth2` extension uses opaque bearer tokens to validate the identity. _technology preview_

### Authorizing Requests

```java
@DenyAll
class Service {}

@GET @Path("authenticated")
@Authenticated
public String getAuthenticated(@Context SecurityContext sec) {}

@GET @Path("secured")
@RolesAllowed("admin")
public String getAdminsOnly() {}

@GET @Path("unsecured")
@PermitAll
public String getUnsecured() {}
```

`javax.ws.rs.core.SecurityContext`

Authorization Through Configuration:

```
quarkus.http.auth.policy.policy1.roles-allowed=user,admin

quarkus.http.auth.permission.permission1.policy=policy1
quarkus.http.auth.permission.permission1.paths=/secured/*
quarkus.http.auth.permission.permission1.methods=GET,POST

quarkus.http.auth.permission.permission2.policy=permit
quarkus.http.auth.permission.permission2.paths=/public/*
```

built-in policies: `permit` and `deny`

### Authentication and Authorization with OIDC

OpenID Connect is an authorization layer added to the OAuth 2.0 protocol. You can use the Quarkus OpenID Connect Extension to secure your JAX-RS applications. OpenID Connect requires an OIDC-compliant and OAuth-compliant authentication server, such as Red Hat Single Sign On (SSO).

Extension: `quarkus-oidc`

```
quarkus.oidc.auth-server-url=http://oidc-server:8180/auth/realms/REALM_NAME
quarkus.oidc.client-id=CLIENT_NAME
quarkus.oidc.credentials.secret=CLIENT_SECRET
```

Define other protection methods by using the `quarkus.oidc.credentials.client-secret.method`

Access to the JWT claims:

```java
@Inject
JsonWebToken jwt;

public SecurityContext debugSecurityContext(@Context SecurityContext ctx) {
    return ctx;
}
```

https://quarkus.io/guides/security-openid-connect

https://quarkus.io/guides/security

Monitoring
-----------------------------------------------------------------------------------------------------------------------------------

Extension : `quarkus-smallrye-metrics`

Metrics data is exposed by REST over HTTP under the `/metrics` base path in two different data formats for HTTP GET requests:

- **JSON format**: the response format when the HTTP Accept header matches application/json.
- **Prometheus text format**: the default response format when the HTTP Accept header does not match a more specific media type, such as application/json.

- _Base metrics_ are the minimum required metrics, outlined in the specification. These base metrics include JVM statistics such as the current heap sizes, garbage collection times, thread counts, and other OS and host system information.

- _Vendor metrics_ include any metrics data on top of the base set of required metrics that the MicroProfile implementation can optionally include. Vendor specific metrics are exposed as a REST endpoint using the relative path `/metrics/vendor`

- _Application metrics_ are custom metrics, which the application developer defines, and are specific to that particular application. An example of an application metric is how many times a specific method is invoked. Path `/metrics/application`.

```java
@Counted(name = "card_transactions", description = "count of credit card transactions")
public void processCreditCardTransaction() {}

@Timed(name = "checksTimer", description = "A measure of how long it takes to perform the primality test.", unit = MetricUnits.MILLISECONDS)
public void timedMethod() {}

@Gauge(name = "concurrent_users", description = "count of active users")
public void listActiveUsers() {}
```

https://github.com/eclipse/microprofile-metrics/blob/master/spec/src/main/asciidoc/app-programming-model.adoc

By default, Quarkus makes the metrics available at the `/metrics` endpoint

To see only the system level metrics, access the `/metrics/base`

https://blog.pvincent.io/2017/12/prometheus-blog-series-part-2-metric-types/

### Tracing

Extension: quarkus-smallrye-opentracing

```
quarkus.jaeger.service-name=myservice
quarkus.jaeger.sampler-type=const
quarkus.jaeger.sampler-param=1
quarkus.jaeger.endpoint=http://localhost:14268/api/traces
quarkus.jaeger.propagation=b3
quarkus.jaeger.reporter-log-spans=true
```

- `quarkus.jaeger.sampler-type` Indicates the rate at which traces and spans should be collected from this application. Options include; `const` (collect all samples), `probabilistic` (random sampling), `ratelimiting` (collect samples at a configurable rate per second), and `remote` (control sampling from a central Jaeger back end). Refer to https://www.jaegertracing.io/docs/1.20/sampling/ for more details.

- `quarkus.jaeger.sampler-param` percentage of requests for which spans should be collected. It should value between 0 and 1. 0 indicates no sample collection, and a 1 indicates collecting all samples (100%)

- `quarkus.jaeger.propagation` Indicates Jaeger to propagate all x-b3-* related headers -> parent-child relationships in the call graph.
- `quarkus.jaeger.reporter-log-spans` Logs span information for all incoming and outgoing traffic from the application.

Enable tracing for all classes and methods in the application. However, for more complex applications you can control which classes and methods must be enabled for tracing.

Annotate classes with the `@Traced` annotation to enable tracing for the entire class. You can also add this annotation at a method level to enable tracing for specific methods and exclude the rest.

For debugging purposes:

```
quarkus.log.console.format=%d{HH:mm:ss} %-5p traceId=%X{traceId}, spanId=%X{spanId}, sampled=%X{sampled} [%c{2.}] (%t) %s%e%n
```

Local Jeager service:

```
podman run --rm --name jaeger \
  -p 5775:5775/udp \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 14268:14268 \
  quay.io/jaegertracing/all-in-one:1.21.0
```

Java Useful Links
-------------

[Mastering generics in java](https://medium.com/@aqilzeka99/mastering-generics-in-java-interview-questions-571232c02af9)