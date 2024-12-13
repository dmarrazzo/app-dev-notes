# Long Producer

## Build and release the image

The following script performs the following actions:

- code is compiled and packaged in a jar in the local machine
- OpenShift builds the image and release it in the local registry
- an imagestream is created to provide an abstraction for referencing container images.
- the imagestream is updated to lookup images from the local registry
- the tag `1.0` is added to the latest image

```sh
mvn clean package
oc new-build --strategy docker --binary --name=long-producer
oc start-build long-producer --from-dir . --follow
oc patch imagestream long-producer --type merge -p '{"spec":{"lookupPolicy":{"local":true}}}'
oc tag long-producer:latest long-producer:1.0
cd ..
```

## Deploy resources

This script deploys resources in OpenShift using _Kustomizer_, triggering the provisioning process:

```sh
oc apply -k k8s
```