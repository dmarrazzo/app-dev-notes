Quarkus
================================================

https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus/2.7

Create project
------------------------------------------------

```sh
mvn com.redhat.quarkus.platform:quarkus-maven-plugin:3.8.6.redhat-00005:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=getting-started \
    -DplatformGroupId=com.redhat.quarkus.platform \
    -DplatformVersion=3.8.6.redhat-00005 \
    -DclassName="org.acme.quickstart.GreetingResource" \
    -Dpath="/hello"
```

Versions of Red Hat Built:

| Release | JDK    | Version                    | OCP  |
|---------|--------|----------------------------|------|
| 3.15.x  | 21     |3.15.1.redhat-00003         |      |
| 3.8.x   | 17,21  | 3.8.6.redhat-00005         | 4.15 |
| 3.2.x   | 17     | 3.2.11.Final-redhat-00001  |      |
| 3.2.x   | 17     | 3.2.10.Final-redhat-00002  |      |
| 3.2.x   | 17     | 3.2.6.Final-redhat-00002   | 4.13 |
| 2.13.x  | 11, 17 | 2.13.8.Final-redhat-00004  | 4.11 |
| 2.7.6   | 11, 17 | 2.7.6.Final-redhat-00006   | 4.10 |
| 2.2.x   | 11, 17 | 2.2.5.Final-redhat-00007   | 4.9  |

Maven BOM https://maven.repository.redhat.com/ga/com/redhat/quarkus/platform/quarkus-bom/maven-metadata.xml

Upstream BOM: https://mvnrepository.com/artifact/io.quarkus/quarkus-bom

Official doc:
https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus/3.8

To skip the platform version config you can configure the quarkus extension registry:

- https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus/3.8/html/getting_started_with_red_hat_build_of_quarkus/assembly_quarkus-getting-started_quarkus-getting-started#proc_configuring-quarkus-developer-tools_quarkus-getting-started

Run in dev
------------------------------------------------

Run in dev mode:

    mvn compile quarkus:dev

Run with staging config: 

    mvn -Dquarkus.profile=staging compile quarkus:dev

In order to debug a problem happening at the startup you can start quarkus in suspend mode:

    mvn quarkus:dev -Dsuspend=true

To run debugger on a different port

    mvn quarkus:dev -Ddebug=5006

Migrate to v3
------------------------------------------------

    mvn com.redhat.quarkus.platform:quarkus-maven-plugin:3.2.10.Final-redhat-00002:update

Uber-jar
------------------------------------------------

Compile:

    mvn package -Dquarkus.package.type=uber-jar

Blogs
------------------------------------------------

[Quarkus: Modernize “helloworld” JBoss EAP quickstart, Part 1](https://developers.redhat.com/blog/2019/11/07/quarkus-modernize-helloworld-jboss-eap-quickstart-part-1/)
[Quarkus: Modernize “helloworld” JBoss EAP quickstart, Part 2](https://developers.redhat.com/blog/2019/11/08/quarkus-modernize-helloworld-jboss-eap-quickstart-part-2/)
[apache camel on quarkus](https://developers.redhat.com/articles/2021/12/06/boost-apache-camel-performance-quarkus)

Extensions
------------------------------------------------

To list available extensions:

    mvn quarkus:list-extensions

To add extensions during project creation:

mvn io.quarkus:quarkus-maven-plugin:1.3.2.Final:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=rest-json-quickstart \
    -DclassName="org.acme.rest.json.FruitResource" \
    -Dpath="/fruits" \
    -Dextensions="resteasy-jsonb"

To add extensions after project has been created:

    mvn quarkus:add-extension -Dextensions="quarkus-jdbc-mysql"

Common configurations
------------------------------------------------

- http port listener:
  
  ```
  quarkus.http.port=8090
  ```

- only in dev profile:
  
  ```
  %dev.quarkus.http.port=8090
  ```

OpenAPI
------------------------------------------------

- Add extension:
  
      mvn quarkus:add-extension -Dextensions="quarkus-smallrye-openapi"

- Properties:
  
      quarkus.swagger-ui.always-include=true

### Forms from Open APIs

Add the following dependency and open the URL http://<host>:<port>/openapi-ui-forms/

```xml
    <dependency>
      <groupId>io.smallrye</groupId>
      <artifactId>smallrye-open-api-ui-forms</artifactId>
      <version>2.1.3</version>
      <scope>runtime</scope>
    </dependency>
```

https://github.com/smallrye/smallrye-open-api/tree/main/ui/open-api-ui-forms

OpenShift
------------------------------------------------

### Maven Driven (Binary 2 Source)

Add extension  -Dextensions="openshift"

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-openshift</artifactId>
    </dependency>

To skip certificates check add the following property:

    quarkus.kubernetes-client.trust-certs=true

To expose the route add the following property:

    quarkus.openshift.route.expose=true

To switch from default DeployConfig to Deployment:

    quarkus.openshift.deployment-kind=Deployment

Deploy the app in the cluster:

    ./mvnw install -Dquarkus.kubernetes.deploy=true

Create the container image and push in the remote registry (build):

    mvn clean package -Dquarkus.container-image.build=true

Resources use OpenShift’s DeploymentConfig:

- Configured to automatically trigger redeployment when detecting ImageStream change
  
  [https://quarkus.io/guides/deploying-to-kubernetes#openshift]()

### OCP driven (Binary 2 Source)

Manual deploy:

1. Uberjar
   
   ```
   mvn clean package -Dquarkus.package.type=uber-jar
   ```

2. Create the build
   
       oc new-build registry.access.redhat.com/openjdk/openjdk-11-rhel7:1.1 --binary --name=people -l app=people

3. Start the build
   
       oc start-build people --from-file target/*-runner.jar --follow

4. Deploy
   
   ```
   oc new-app people && oc expose svc/people
   ```
   
   Check the rollout
   
       oc rollout status -w deployment/people

Dockerfile approach: https://github.com/vaibhavjainwiz/rhpam-cloud-enablement

Alternative approach: drang and drop the uber jar in the OpenShift Console

### Managing environment variables

This config load env from configmap:

```
quarkus.openshift.env.configmaps=app-config
```

In order to automate the configmap creation add the following file `src/main/kubernetes/openshift.yml`, with the following content:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  labels:
    app: people-app
data:
  QUARKUS_DATASOURCE_JDBC_URL: "jdbc:postgresql://postgresql:5432/people"
  QUARKUS_DATASOURCE_USERNAME: "user"
  QUARKUS_DATASOURCE_PASSWORD: "postgres"
```

Make sure that the env variables are defined in the `application.properties`.

See more: https://quarkus.io/guides/deploying-to-kubernetes#using-existing-resources

### Deploy using jib

expose the OCP registry:

```sh
oc patch configs.imageregistry.operator.openshift.io/cluster --patch '{"spec":{"defaultRoute":true}}' --type=merge
```

Get the registry URL:

```sh
oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}'
```

Add in application.properties:

```
quarkus.container-image.group=osl
quarkus.container-image.name=serverless-workflow-order
quarkus.container-image.tag=1.0
quarkus.container-image.registry=default-route-openshift-image-registry.apps.ocp4.dmshift.eu
```

Make sure that group match the project name.

```sh
mvn install -Dquarkus.container-image.push=true -Dquarkus.container-image.username=$(oc whoami) -Dquarkus.container-image.password=$(oc whoami -t)
```


### Test the application

- Get the route
  
      PEOPLE_ROUTE_URL=$(oc get route people -o=template --template='{{.spec.host}}')
  
  or:
  
    PEOPLE_ROUTE_URL=$(oc get route discount -o jsonpath='{.spec.host}')

- Run
  
      curl http://${PEOPLE_ROUTE_URL}/hello

### Health check config

    oc set probe deployment/people --readiness --initial-delay-seconds=30 --get-url=http://:8080/health/ready
    
    oc set probe deployment/people --liveness --initial-delay-seconds=30 --get-url=http://:8080/health/live

### Native deploy

```
mkdir build
cp src/main/docker/Dockerfile.native-micro build/Dockerfile
cd build/
mkdir target
cp ../target/myapp-1.0.0-SNAPSHOT-runner target/
oc start-build myapp-native --from-dir . --follow
oc get is
oc new-app myapp-native
oc get pods
oc expose svc/myapp-native
oc get route
```

### Source to image

Compile from git repo source.

1. Open the `pom.xml` file, and change the Java configuration to version 17, as follows:

   ```xml
   <maven.compiler.source>17</maven.compiler.source>
   <maven.compiler.target>17</maven.compiler.target>
   ```

1. Create a hidden directory called `.s2i` at the same level as the pom.xml file.

2. Create a file called `environment` in the `.s2i` directory and add the following content:

   ```
   MAVEN_S2I_ARTIFACT_DIRS=target/quarkus-app
   S2I_SOURCE_DEPLOYMENTS_FILTER=app lib quarkus quarkus-run.jar
   JAVA_OPTIONS=-Dquarkus.http.host=0.0.0.0
   AB_JOLOKIA_OFF=true
   JAVA_APP_JAR=/deployments/quarkus-run.jar
   ```

3. Commit and push your changes to the remote Git repository.

4. To import the supported OpenShift image, enter the following command:

   ```
   oc import-image --confirm ubi8/openjdk-17 --from=registry.access.redhat.com/ubi8/openjdk-17
   ```

5. To build the project in OpenShift, enter the following command where <git_path> is the path to the Git repository that hosts your Quarkus project and <project_name> is the OpenShift project that you created.

   ```
   oc new-app ubi8/openjdk-17 <git_path> --name=<project_name>
   ```

6. This command builds the project, creates the application, and deploys the OpenShift service.

7. To deploy an updated version of the project, push any updates to the Git repository then enter the following command:

   ```
   oc start-build <project_name>
   ```

### Remote dev and debug

To enable remote dev:

* Build a mutable application: using the mutable-jar format. 
   Set the following properties in `application.properties`:

   ```
   quarkus.package.type=mutable-jar
   quarkus.live-reload.password=changeit!!!
   quarkus.live-reload.url=https://my.cluster.host.com
   quarkus.openshift.env.vars.quarkus-launch-devmode=true
   quarkus.openshift.env.vars.java-debug=true
   ```

   > **NOTE**: Before you start Quarkus on the remote host set the environment variable `QUARKUS_LAUNCH_DEVMODE=true`. Last line address this if you deploy via `quarkus-openshift` plugin.

To debug the remote pod:

1. Make sure that the following enviroment variable is properly set: `JAVA_DEBUG=true`
2. In a terminal launch the port forwarding `oc port-forward deployment/<deployment-name> 5006:5005`
3. Attach the debugger to localhost:5006

Dev UI can work on the remote pod disabling CORS:

```yaml
      env:
      - name: QUARKUS_DEV_UI_CORS_ENABLED
        value: "false"
```

Development
---------------------------------------------------------

### Environment variables

Code:

```java
    @ConfigProperty(name = "producer.tick-frequency", defaultValue="1000") 
    private Long tickFrequency;
```

Config map:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kafka-producer-config
  labels:
    app: kafka-producer
data:
  KAFKA_BOOTSTRAP_SERVERS: my-cluster-kafka-bootstrap.my-kafka.svc:9092
  PRODUCER_TICK_FREQUENCY: "100"
```

Testcontainers with podman
---------------------------------------------------------

**Warning** this procedure was tested in Fedora 34 using _Fish shell_.

Requirements:

```
podman-3.4.0-1.fc34.x86_64
podman-docker-3.4.0-1.fc34.noarch
```

Disable the registry prompt by setting the `short-name-mode="disabled"` configuration property of Podman in `/etc/containers/registries.conf`

Start Podman service to listen on socket and grant access to users:

```sh
sudo systemctl start podman.socket
systemctl --user enable podman.socket --now
```

The following commands to check that all is up and running:

```
podman-remote info
curl -H "Content-Type: application/json" --unix-socket /run/user/(id -u)/podman/podman.sock http://localhost/_ping
```

Set the following environment variables (Fish shell way):

```sh
set DOCKER_HOST unix:///run/user/(id -u)/podman/podman.sock
```

In case of problems you can try diabling RYUK

```shell
set TESTCONTAINERS_RYUK_DISABLED true
```

Create `~/.testcontainers.properties` and the following properties:

```
docker.host = unix\:///run/user/1000/podman/podman.sock
ryuk.container.privileged = true
```

Enable `app` in selinux?

Reset everything:

```
podman system reset --force
```

See:

- https://golang.testcontainers.org/system_requirements/using_podman/

- https://github.com/quarkusio/quarkusio.github.io/blob/aeeeaf3a4e68012ea8cbefb1e3bf9e1a6a6b376f/_posts/2021-11-02-quarkus-devservices-testcontainers-podman.adoc

- https://www.redhat.com/sysadmin/podman-docker-compose

- https://www.testcontainers.org/features/configuration/



## Workshop

For more information here - [Quarkus Workshop RHPDS Provisioning Guide:](https://docs.google.com/document/d/1rZ6RoBKVu94POoegPLdJXLQ1Jor2mHWcPQ827nNKISE)

24 X 7 X 365 lab instructions(Only Text) here:

Module 1: https://bit.ly/quarkus-workshop-m1  

Module 2: https://bit.ly/quarkus-workshop-m2  

Module 3: https://bit.ly/quarkus-workshop-m3   

It uses this stack:

- Red Hat Build of Quarkus (RHBQ) 2.2.5.Final-redhat-00007

- CodeReady Workspaces 2.15.2

- Red Hat Integration - AMQ Streams 2.0.1-1

- Red Hat Single Sign-On Operator 7.5.1

- Distributed Tracing 1.30.0

## Tuning

[JVM Options Configuration Tool](https://access.redhat.com/labs/jvmconfig/)