# OpenShift

oc login

oc get

oc create -f pod.yaml

oc delete pod quotes-ui

oc logs react-ui

Use the oc explain command to get information about valid fields for an object. For example, execute oc explain pod to get information about possible Pod object fields. You can use the YAML path to get information about a particular field, for example:

    oc explain pod.metadata.name

Label can be used for selecting:

    oc get pod --selector group=developers

Create Pods Declaratively

    kind: Pod
    apiVersion: v1
    metadata:
    name: example-pod
    namespace: example-project
    spec:
    containers:
    - name: example-container
        image: quay.io/example/awesome-container
        ports:
        - containerPort: 8080
        env:
        - name: GREETING
        value: "Hello from the awesome container"

Create Pods Imperatively

    oc run example-pod \
    --image=quay.io/example/awesome-container \
    --env GREETING='Hello from the awesome container' \
    --port=8080


Create Services Declaratively

    apiVersion: v1
    kind: Service
    metadata:
    name: backend
    spec:
    ports:
    - port: 8080
        protocol: TCP
        targetPort: 8080
    selector:
        app: backend-app


Create Services Imperatively

    oc expose pod backend-app \
        --port=8080 \
        --targetPort=8080 \
        --name=backend-app

`--dry-run=client -o yaml` options to generate a service definition

## Deploy Pods with Deployments

Deployment object is a Kubernetes controller that manages pods

Managing Pod Scaling

Managing Application Changes

Managing Application Updates

    apiVersion: apps/v1
    kind: Deployment
    metadata:
    labels:
      app: deployment-label
    name: example-deployment
    spec:
    replicas: 3
    selector:
      matchLabels:
        app: example-deployment
    strategy: RollingUpdate
    template:
      metadata:
        labels:
            app: example-deployment
      spec:
        containers:
        - image: quay.io/example/awesome-container
            name: awesome-pod


The .spec.template.spec field corresponds to the .spec field of the Pod object.

Imperative:

    oc create deployment example-deployment \
      --image=quay.io/example/awesome-container \
      --replicas=3

WARNING: `.spec.selector.matchLabels` field must be a subset of labels of the `.spec.template.metadata.labels` field.

## Expose Applications for External Access

    apiVersion: route.openshift.io/v1
    kind: Route
    metadata:
      labels:
        app: app-ui
      name: app-ui
      namespace: awesome-app
    spec:
      port:
        targetPort: 8080
      host: ""
      to:
        kind: "Service"
        name: "app-ui"

Imperative:

    oc expose service app-ui
