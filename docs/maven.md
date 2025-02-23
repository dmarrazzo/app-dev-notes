Maven survival guide
====================

## Create an empty project

```sh
mvn archetype:generate \
	-DgroupId=com.redhat.example \
	-DartifactId=emp-onboarding-model \
	-DarchetypeArtifactId=maven-archetype-quickstart \
	-DinteractiveMode=false
```

## Compilation setting (build)
The following configuration set the **JDK level** and **exclude** unwanted files

```xml
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration>
          <source>${maven.compiler.source}</source>
          <target>${maven.compiler.target}</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

Extra configuration to exclude a file pattern from compilation:

```xml
          <excludes>
            <exclude>**/.*.java</exclude>
          </excludes>
```

## Dependency Scope

Dependency scope is used to limit the transitivity of a dependency, and also to affect the classpath used for various build tasks.

There are 6 scopes available:

- **compile**  
This is the default scope, used if none is specified. Compile dependencies are available in all classpaths of a project. Furthermore, those dependencies are propagated to dependent projects.
- **provided**  
This is much like compile, but indicates you expect the JDK or a container to provide the dependency at runtime. For example, when building a web application for the Java Enterprise Edition, you would set the dependency on the Servlet API and related Java EE APIs to scope provided because the web container provides those classes. This scope is only available on the compilation and test classpath, and is not transitive.

## Javadoc
In Eclipse go to Windows-> Preferences-> Maven. Check the box that says "Download Artifact Javadoc."

## Dependency Version

See the [POM Syntax section of the Maven book][1] for more details. Or see this doc on Dependency Version Ranges, where:

A square bracket ( [ & ] ) means "closed" (inclusive).
A parenthesis ( ( & ) ) means "open" (exclusive).

Declare an open-ended version range (will resolve to 2.0.0):

    <version>[1.0.0,)</version>


[1]: http://www.mojohaus.org/versions-maven-plugin/examples/resolve-ranges.html

## Dependency utilities

It can be really useful to understand the dependencies chains looking at the dependency tree:

	mvn dependency:tree 

The following commmand analyzes the dependencies and highlights the unused and missing ones:

	mvn dependency:analyze

To copy in the target directory all the dependencies:

	mvn dependency:copy-dependencies

## Configure Maven Repository

### Off line repositories

Configure the file system

- Download and unzip official maven repo
- Change the maven `setting.xml` to point that

### On line repositories

There are 2 online repositories:

 - [https://maven.repository.redhat.com/ga]()
 - [https://maven.repository.redhat.com/earlyaccess/all/]()

**Further information:** in the chapter *Maven Repository* of the *Installation Guide*.

The following command pull all the dependencies in local repository:

    mvn dependency:go-offline

## How to

### Check artefact existence


    mvn dependency:get -Dartifact=g:a:v

To check a specific repo

    mvn dependency:get -Dartifact=g:a:v -o -DrepoUrl=http://192.168.1.200:8080/business-central/maven2/

To download an artifact locally (e.g. retrieve the jar). In the following example how to retrieve with a specific classifier

	mvn dependency:get -Dartifact=org.kie.kogito:jobs-service-inmemory:1.15.0.Final:jar:runner -Dtransitive=false -Ddest=jobs-service-inmemory-1.15.0.Final-runner.jar



### Skip Tests

    mvn install -DskipTests


### Force dependency download

Command line:

    mvn package -U

`-U` means force update of dependencies.

In eclipse, there is a properties in the Run Configuration...

### Problem eclipse cannot find the dependency

Sometime even if the maven builds successfully it raise some warnings about maven resources that has an erroneous header.

E.g.

    [WARNING] error reading /home/donato/.m2/repository/org/jbpm/jbpm-flow/6.3.0.Final-redhat-5/jbpm-flow-6.3.0.Final-redhat-5.jar; invalid LOC header (bad signature)

Eclipse is not able to retrieve the maven dependency, and many compile errors occurs.
The resolution is manually delete the maven dep from .m2 repository and let maven download it again.

### Embed external libraries

 - copy the jar in the resources
 - add dependencies

        <dependency>
            <groupId>example</groupId>
            <artifactId>indennizzo-wsgen</artifactId>
            <version>1.0</version>
            <scope>system</scope>
            <systemPath>${project.basedir}/src/main/resources/poc.jar</systemPath>
        </dependency>
### Execute the program

Add to the `pom.xml`

	<build>
		<plugins>
			<plugin>
				<groupId>org.codehaus.mojo</groupId>
				<artifactId>exec-maven-plugin</artifactId>
				<version>1.2.1</version>
				<!-- WARNING: This configuration must be run with "mvn exec:java" not "mvn exec:exec". -->
				<!-- It is impossible to write a configuration that is compatible with both exec:java and exec:exec -->
				<executions>
					<execution>
						<goals>
							<goal>java</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<mainClass>client.Main</mainClass>
					<arguments>
						<argument>-Xms256m</argument>
					</arguments>
				</configuration>
			</plugin>
		</plugins>
	</build>

then issue the following command:

    mvn exec:java 

Optionally, it's possible to override configuration adding following parameters:

    -Dexec.mainClass=com.yourcompany.yourclass -Dexec.args="arg1 arg2 arg3" -Dkey=value

### Config the manifest main class


	<plugin>
		<groupId>org.apache.maven.plugins</groupId>
		<artifactId>maven-jar-plugin</artifactId>
		<version>2.6</version>
		<extensions>false</extensions>
		<inherited>true</inherited>
		<configuration>
			<archive>
				<manifest>
					<mainClass>client.Main</mainClass>
				</manifest>
			</archive>
		</configuration>
	</plugin>


# Build a jar with all dependencies

```
<!-- single jar with dep: mvn assembly:single -->
<plugin>
	<artifactId>maven-assembly-plugin</artifactId>
	<version>3.1.1</version>
	<executions>
		<execution>
		<id>make-assembly</id> <!-- this is used for inheritance merges -->
		<phase>package</phase> <!-- bind to the packaging phase -->
		<goals>
			<goal>single</goal>
		</goals>
		</execution>
	</executions>
	<configuration>
		<descriptorRefs>
			<descriptorRef>jar-with-dependencies</descriptorRef>
		</descriptorRefs>
		<archive>
			<manifest>
				<mainClass>client.EmbedMain</mainClass>
			</manifest>
		</archive>
	</configuration>
</plugin>		
```

### Bill of Material (BOM)

The BOM is a way to manage the version in a single place, add to the POM file:

```xml
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>com.redhat.ba</groupId>
				<artifactId>ba-platform-bom</artifactId>
				<version>${ba.version}</version>
				<scope>import</scope>
				<type>pom</type>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

### Add all the product libraries without the version information

E.g. Some basic lib

```xml
	<dependency>
		<groupId>org.kie</groupId>
		<artifactId>kie-api</artifactId>
		<scope>provided</scope>
	</dependency>
```

#### Logging

it is important for all Java plain execution:

		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
			<scope>provided</scope>
		</dependency>
		<dependency>
			<groupId>ch.qos.logback</groupId>
			<artifactId>logback-classic</artifactId>
			<scope>provided</scope>
		</dependency>


### Example of other dependencies

#### Java EE 

**Warning: do not use in bpm project**

    <dependency>
      <groupId>javax</groupId>
      <artifactId>javaee-api</artifactId>
      <version>6.0</version>
    </dependency>


## Internal Maven repository

Business Central hosts an internal maven repository.
You can see and upload new artifacts of the Internal repository from the UI:

* Authoring -> Artifact repository

The upload can be automated with following procedure (acknowledgements to Anton Gerli)

- in the project pom.xml add

        <distributionManagement>
            <repository>
                <id>guvnor-m2-repo</id>
                <name>maven repo</name>
                <url>http://localhost:8080/business-central/maven2/</url>
                <layout>default</layout>
            </repository>
        </distributionManagement>

- Configure server connection in your `~/.m2/settings.xml`

        <server>
            <id>guvnor-m2-repo</id>
            <username>user</username>
            <password>password</password>
            <privateKey>prdprivatekey</privateKey>
            <configuration>
                <wagonProvider>httpclient</wagonProvider>
                <httpConfiguration>
                    <all>
                        <usePreemptive>true</usePreemptive>
                    </all>
                </httpConfiguration>
            </configuration>
        </server>

- deploy

	mvn deploy

- To manage the username and passoword as command line paramiters, use variables in `settings.xml` file. 

```xml
<servers>
    <server>
        <id>guvnor-m2-repo</id>
        <username>${user}</username>
        <password>${password}</password>
    </server>
</servers>
```

Then, provide explicit values when invoking to Maven:

	mvn -Duser=... -Dpassword=... deploy

**References:**

[Uploading Artifacts to Maven Repository](https://access.redhat.com/documentation/en-us/red_hat_jboss_bpm_suite/6.4/html/development_guide/chap_maven_dependencies#uploading_artifacts_to_maven_repository)


### To configure the internal maven directory

Configure the local repository in settings.xml:

<localRepository>/opt/jboss/.m2/repository</localRepository>


### Deploy a project from command line (maven)

In your **pom.xml**

    <distributionManagement>
        <repository>
            <id>guvnor-m2-repo</id>
            <name>maven repo</name>
            <url>http://localhost:8080/business-central/maven2/</url>
            <layout>default</layout>
        </repository>
    </distributionManagement>

Alternatively add the profile in **`~/.m2/settings.xml`**:

```xml
<profile>
	<id>business-central</id>
	<repositories>
	<repository>
		<id>guvnor-m2-repo</id>
		<url>http://localhost:8080/business-central/maven2/</url>
		<layout>default</layout>
		<releases>
		<enabled>true</enabled>
		<updatePolicy>always</updatePolicy>
		</releases>
		<snapshots>
		<enabled>true</enabled>
		<updatePolicy>always</updatePolicy>
		</snapshots>
	</repository>
	</repositories>
</profile>
```

In your **`~/.m2/settings.xml`**, add this `<server>` element to the `<servers>` section:

```xml
  <servers>
    <server>
        <id>guvnor-m2-repo</id>
        <username>${repo.login}</username>
        <password>${repo.pwd}</password>
        <configuration>
            <wagonProvider>httpclient</wagonProvider>
            <httpConfiguration>
                <all>
                    <usePreemptive>true</usePreemptive>
                </all>
            </httpConfiguration>
        </configuration>
    </server>
  </servers>
```

Now you can deploy with maven command line:

```sh
$ mvn deploy -Drepo.login=user -Drepo.pwd=password 
```

If you have not specified `<distributionManagement>` in the `pom.xml` file, then use this command:

```sh
$ mvn deploy -Drepo.id=guvnor-m2-repo -Drepo.login=user -Drepo.pwd=password -DaltDeploymentRepository=guvnor-m2-repo::default::http://<remote-host>/maven2/
```

Here a complete example of maven [settings.xml](config/settings.xml)
