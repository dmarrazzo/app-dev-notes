# OpenShift Container Platform Cheatsheet

### Education

Red Hat OpenShift I: Containers & Kubernetes (DO180)

## Create SSL secrets

[Generate_a_SSL_Encryption_Key_and_Certificate](https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.1/html-single/Security_Guide/index.html#Generate_a_SSL_Encryption_Key_and_Certificate)

    keytool -genkeypair -alias jboss -keyalg RSA -keystore keystore.jks -storepass mykeystorepass --dname "CN=dmarrazzo,OU=Sales,O=redhat.com,L=Rome,S=RM,C=Italy"
    oc create secret generic kieserver-app-secret --from-file=keystore.jks
    oc create secret generic businesscentral-app-secret --from-file=keystore.jks    

## Create credential secret

    oc create secret generic rhpam-credentials --from-literal=KIE_ADMIN_USER=pamadmin --from-literal=KIE_ADMIN_PWD=adminPassword

Get the result without creating:

    oc create secret generic rhpam-credentials --dry-run=client  --from-literal=KIE_ADMIN_USER=pamadmin --from-literal=KIE_ADMIN_PWD=adminPassword -o yaml

## Extract secrets

    oc get secret my-cluster-clients-ca-cert -o jsonpath='{.data.ca\.crt}' | base64 --decode

easier:

    oc extract secret/my-cluster-cluster-ca-cert --keys=ca.crt --to=- > kafka-cluster.crt

### Optionally import a self signed certificate

    keytool -import -v -trustcacerts -alias ALIAS_NAME -file CERT_FILE \
        -keystore keystore.jks -keypass PASSWORD -storepass PASSWORD

Replace the keystore:

    oc create secret generic kieserver-app-secret --from-file=keystore.jks --dry-run -o yaml | oc replace -f -


## Create the app

### Local build

```sh
cd weather
mvn clean package
# alternatively use docker command line
buildah build -f src/main/docker/Dockerfile.jvm -t quarkus/otel-weather .
```

### Binary Build

```sh
oc new-build --strategy docker --binary --name=app-name -l app=app-name

mkdir build
mv target build
cp src/main/docker/Dockerfile.jvm build/Dockerfile
cd build/
oc start-build app-name --from-dir . --follow

```

## Expose a service - create a new route

In order to get an accessible URL:

  oc expose service/openshift-kie-springboot --port=8090

## Expose git ssh

Expose all services:

    oc expose dc myapp-rhpamcentr --type=LoadBalancer --name=rhpamcentr-exp

Delete service:

    oc delete svc/rhpamcentr-exp

Check the `NodePort` for `TargetPort 8001` with following command:

    oc describe svc/rhpamcentr-exp

Example of output:

    TargetPort:               8001/TCP
    NodePort:                 port-4  32618/TCP

You can access to the internal git in this way:

    git clone ssh://pamadmin@$(minishift ip):32618/<project path>

Alternatively, you can forward the pod port:

1. Find the pod name:
   
        oc get pods

2. Forward the port to your localhost
   
        oc port-forward myapp-rhpamcentr-5-pfd7l 8001

References:

[Exposing Services](https://docs.okd.io/latest/minishift/openshift/exposing-services.html)

# Explore resources

## Get more details

    oc get pods -o wide

## Get with template

    oc get pod --selector app=training -o template --template '{{range .items}}{{.metadata.name}} {{.status.podIP}}{{"\n"}}{{end}}'

# Fine tunining

## System properties

Add custom system properties

    JAVA_OPTS_APPEND=-Dkubernetes.websocket.timeout=10000
    
    JAVA_OPTS_APPEND=-XX:MetaspaceSize=512M

## Memory issues

[How to change JVM memory options using Red Hat JBoss EAP image for Openshift](https://access.redhat.com/solutions/2682021)

Environment variables:

    CONTAINER_HEAP_PERCENT = 0.5
    INITIAL_HEAP_PERCENT = 0.5

Metaspace (works out of S2I?): 

    GC_MAX_METASPACE_SIZE = 512

## timeout

    kubernetes.websocket.timeout

## Grant permission to use operators

oc adm policy add-cluster-role-to-user cluster-reader developer

## Docker image hacking

docker run -ti quay.io/rhpam_rhdm/rhpam-businesscentral-rhel8-cm-showcase:7.5.0 /bin/sleep infinity

config directory: `/opt/eap/bin/launch`

## ConfigMap for properties

    oc create configmap const-props --from-file=use-case-3/const.propertie
    oc set volume dc/rhpam-authoring-kieserver --add --name=config-volume --type=configmap --configmap-name=const-props --mount-path=/etc/config

## Investigating pod issues

https://docs.openshift.com/container-platform/4.5/support/troubleshooting/investigating-pod-issues.html

## ConfigMap for Business Calendar

    oc create configmap jbpm-business-calendar-props --from-file=jbpm.business.calendar.properties

Then added it the Business Central Deployment Configuration:

    oc set volume dc/rhpam-trial-rhpamcentr --add --name=jbpm-business-calendar-volume --type=configmap --configmap-name=jbpm-business-calendar-props --mount-path=/deployments/ROOT.war/WEB-INF/classes

## replace a file

Use the subpath and the full path to the file

```yaml
    volumeMounts:
      - name: log4j-properties-volume
        mountPath: /zeppelin/conf/log4j.properties
        subPath: log4j.properties

    volumeMounts:
      - name: log4j-properties-volume
        mountPath: /zeppelin/conf
```

## intenal hostname

internal namespace convention:

    my-svc.my-namespace.svc.cluster.local

how to retrive the internal hostname for a given service:

    oc get svc my-cluster-kafka-bootstrap -o go-template --template='{{.metadata.name}}.{{.metadata.namespace}}.svc{{println}}'

## External port for a given pod

    oc get pod my-cluster-kafka-0 -o jsonpath="{.spec.containers[*].ports[*].containerPort}"

## Delete all PVC

    oc get pvc -o jsonpath="{.items[*].metadata.name}{'\n'}" |xargs oc delete pvc

for a given label:

    oc delete pvc --selector="strimzi.io/cluster=my-cluster"

# Node labels

Useful to define availability zones

## Add a label to the nodes

    oc label node master-0 topology.kubernetes.io/zone=dc1

## Show label along with nodes 

    oc get node -L topology.kubernetes.io/zone

## Deployment affinity

```yaml
    spec:
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
            - matchExpressions:
              - key: topology.kubernetes.io/zone
                operator: In
                values:
                - dc2
```

## Image Registry

List images:

    oc get images

Show images timestamp:

    oc get images -o go-template='{{range .items}}{{.metadata.name}} {{.metadata.creationTimestamp}} {{.dockerImageReference}} {{"\\n"}}{{end}}'

An Image Stream is a resource that provides an abstraction layer for container images. It allows you to reference images in a consistent way, regardless of where they are stored.

List Image Streams:

    oc get imagestreams

To link the Image Stream to an image you have to add a tag:

    oc tag <image fully qualified url> <IMAGE_STREAM_NAME>:<TAG>

Example:

    oc tag image-registry.openshift-image-registry.svc:5000/my-kafka/kafka-consumer@sha256:d612fb6411d5da26a90104ba975c20a4070caad274789e6e698c0f3738e10ffe kafka-consumer:1.0.0-SNAPSHOT

The `oc tag` command itself doesn't delete tags from an image stream in OpenShift. Here's how you can achieve that:

**Deleting a Single Tag:**

Use the `oc delete istag <image_stream_name>:<tag_name>` command. Replace `<image_stream_name>` with the actual name of your image stream and `<tag_name>` with the specific tag you want to remove.

```
$ oc delete istag my-image-stream:v1.0
```

**Deleting Multiple Tags:**

There are two options for deleting multiple tags:

1. **Wildcards:** You can use wildcards with the `oc delete istag` command to target multiple tags that follow a specific pattern. For example, to delete all tags starting with "v-" in the "my-image-stream" image stream, use:

```
$ oc delete istag my-image-stream:v-*
```

**Caution:** Be careful when using wildcards, as it can accidentally delete unintended tags.

2. **Looping:** If you have a list of specific tags to delete, you can loop through them using a scripting language like bash and the `oc delete istag` command within the loop.

**Important Note:**

Deleting a tag doesn't necessarily remove the underlying image data from the registry. OpenShift employs an image garbage collection process that periodically cleans up unused images. To manually trigger garbage collection, you can use the `oc adm images prune` command (requires admin privileges).

### Expose Internal Registry

Create the route:

    oc patch configs.imageregistry.operator.openshift.io/cluster --patch '{"spec":{"defaultRoute":true}}' --type=merge

Get registry endpoint:

    export REGISTRY=`oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}'`

or:

    set REGISTRY (oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')


### Push local image

podman login:

    podman login -u (oc whoami) -p (oc whoami --show-token) $REGISTRY

tag the image:

    podman tag image-name ${REGISTRY}/<insert OCP project>/image-name:latest

Push the image:

    podman push ${REGISTRY}/<insert OCP project>/image-name:latest

# Troubleshooting

## Retrive a file from a pod

Retrive the configuration xml:

    oc rsync <pod-name>:/opt/eap/standalone/configuration/standalone-openshift.xml .

## Cluster Certificate refresh

1. Connect to the bastion

2. Check the csr conditions:
   
     $ oc get csr
   
   Look for "Pending" in the output.

3. Try the below to approve, followed with a review of the command:
   
     $ oc get csr -o name | xargs oc adm certificate approve
     $ oc get csr
   
   Stuck operators. The below will get you status. You should see the operators with "True", "False", "False". If you don't then try the following command and review again:
   
     $ oc get co
     $ oc delete secret csr-signer csr-signer-signer -n openshift-kube-controller-manager-operator

### new project

    oc new-project hello-openshift \
        --description="This is an example project to demonstrate OpenShift v3" \
        --display-name="Hello OpenShift"

### pod list

    oc get pods

### restart the server (delete the pod)

In other words, you have to delete the pod, in this way OCP will create and start a new one

    oc delete pod <podname>

### Application exposed URLs

    oc get routes

### environment variables

List all

    oc set env dc/myapp-rhpamcentr --list

### get secret config

    oc get secrets businesscentral-app-secret -o=yaml

### get details in yaml

    oc get bc cakephp-mysql-example -o yaml | less

### Clean up

- delete the application
  
      oc delete all -l app=rhpam72-authoring

- delete all the project
  
      oc delete all -l application=pam72

- delete all the old pods

  ```sh
  oc get pods|egrep "Error|Completed" | awk '{ print "pod/"$1 }' | xargs oc delete
  ```
  or

  ```sh
  oc get pods -o jsonpath="{.items[?(@.status.containerStatuses[0].started==false)].metadata.name}{'\n'}"| xargs oc delete pod 
  ```

- Deleting completed pods

  ```sh
  kubectl delete pods --field-selector=status.phase=Succeeded
  ```

- Deleting failed pods

  ```sh
  kubectl delete pods --field-selector=status.phase=Failed
  ```

### scale up and down

  oc scale dc/pam-dev-rhpamcentr --replicas=0
  oc scale dc/pam-dev-kieserver --replicas=0

### server log

    oc log -f <pod-name>

### get a file the container

    oc rsync <existing db container with db archive>:/var/lib/mysql/data/db_archive_dir /tmp/

### change the probe

    oc set probe dc/pam72-kieserver --readiness --initial-delay-seconds=90 --all deploymentconfig.apps.openshift.io/pam72-kieserver probes updated

### use go-template to extract information

extract all names:

    oc get pods --selector=job-name=k6-test -o go-template='{{range .items}}{{.metadata.name}}{{"\\n"}}{{end}}' 

extract list of key value pairs 

    oc get knativeserving.operator.knative.dev/example -n knative-serving --template='{{range .status.conditions}}{{printf "%s=%s\n" .type .status}}{{end}}'


### Add extra system properties

Add environment variable:

    JAVA_OPTS_APPEND = "-Dfile.encoding=UTF-8 -Dfile.io.encoding=UTF-8 -Dclient.encoding=UTF-8 -DjavaEncoding=UTF-8 -Dorg.apache.catalina.connector.URI_ENCODING=UTF-8"

When the deployment is handled by the operator, it's possible to leverage the `jvm` section:

```
apiVersion: app.kiegroup.org/v2
kind: KieApp
metadata:
  generation: 2
  name: rhpam-trial
  namespace: demo-pam-operator
  selfLink: /apis/app.kiegroup.org/v2/namespaces/demo-pam-operator/kieapps/rhpam-trial
  uid: 9497c82e-edac-419a-b2f7-a6a92970ebce
spec:
  environment: rhpam-trial
  objects:
    servers:
      - jvm:
          javaOptsAppend: >-
            -Dorg.kie.server.xstream.enabled.packages=org.drools.persistence.jpa.marshaller.*
```

### Raise log level

Procedure to raise the PAM log level for the Web Services handler (ephemeral change):

1. Login in your OCP and get pod name for the kie-server

```sh
oc login -u <user>
oc project <project>
oc get pods
[...]
```

2. Open a shell in the pod:
   
        oc rsh <kieserver-pod-name>

3. Start the EAP command line and issue the commands as in the example:
   
        sh-4.2$ cd /opt/eap/bin/
        sh-4.2$ ./jboss-cli.sh --connect controller=localhost:9990
        [standalone@localhost:9990 /] /subsystem=logging/logger=org.jbpm.process.workitem.webservice/:add(category=org.jbpm.process.workitem.webservice,level=DEBUG,use-parent-handlers=true)
        [standalone@localhost:9990 /] /subsystem=logging/console-handler=CONSOLE:change-log-level(level=DEBUG)
        {"outcome" => "success"}
        [standalone@localhost:9990 /] quit 

4. Close the remote shell
   
    sh-4.2$ exit

*From version 7.5* It's possible to add the logging via environment variables: 

LOGGER_CATEGORIES=org.kie:DEBUG, org.drools:DEBUG, org.jbpm:DEBUG

More info: https://github.com/jboss-container-images/jboss-eap-modules/blob/EAP_724_OPENJDK11_CR2/os-eap7-launch/added/launch/configure_logger_category.sh

### force a new deployment

You can start a new deployment process manually using the web console, or from the CLI:

    oc rollout latest dc/<name>

### kill forcefully the pod

    # oc delete pod example-pod-1 -n name --grace-period=0

or 

    # oc delete pod example-pod-1 -n name --grace-period=0 --force

### Remove CPU limits

  oc get limitrange
  oc delete limitrange/<name>

### Max cpu allocate

    oc get node <node-name> -o yaml|grep allocatable -A11

### Enabling monitoring for user-defined projects

```sh
echo "apiVersion: v1
kind: ConfigMap
metadata:
  name: cluster-monitoring-config
  namespace: openshift-monitoring
data:
  config.yaml: |
    enableUserWorkload: true" | oc -n openshift-monitoring apply -f -
```

Check:

```sh
oc -n openshift-user-workload-monitoring get pod
```


Enable users:

- https://docs.openshift.com/container-platform/4.9/monitoring/enabling-monitoring-for-user-defined-projects.html

### checking network connections

netstat for a pod:

https://access.redhat.com/solutions/7011679

```
$ oc debug node/worker-2
# NAME=<pod-name>
# NAMESPACE=<pod-namespace>
# pod_id=$(chroot /host crictl pods --namespace ${NAMESPACE} --name ${NAME} -q)
# ns_path="/host/$(chroot /host bash -c "crictl inspectp $pod_id | jq '.info.runtimeSpec.linux.namespaces[]|select(.type==\"network\").path' -r")"
# nsenter_parameters="--net=${ns_path}"
# nsenter $nsenter_parameters -- netstat -anptu
```

## Openshift Useful links

- [https://docs.openshift.com/container-platform/3.9/dev_guide/copy_files_to_container.html]()

- [https://www.mankier.com/package/origin-clients]()

- [Learn OpenShift interactively](https://learn.openshift.com/)
