# Introduction to Containers with Podman

Building and managing containers with Podman for deployment on a Kubernetes and OpenShift 4 cluster.

## Containers

**Container image** contains effectively immutable data that defines an application and its libraries. 

Container images can be used to create **Container instances**, which are executable versions of the image that include references to networking, disks, and other runtime necessities.

**VM** is useful when an additional full computing environment is required, such as when an application requires specific, dedicated hardware. Additionally, a VM is preferable when an application requires a non-Linux operating system or a different kernel from the host.

## Kubernetes Features

Service discovery and load balancing

Horizontal scaling

Self-healing

Automated rollout

Secrets and configuration management

Operators: Operators are packaged Kubernetes applications that also bring the knowledge of the application’s lifecycle into the Kubernetes cluster.

## Red Hat OpenShift Container Platform (RHOCP) 

is a set of modular components and services that are built on top of the Kubernetes container infrastructure. 

Red Hat OpenShift Container Platform Features

- Developer workflow - Integrates a built-in container registry, Continuous Integration/Continuous Delivery (CI/CD) pipelines, and Source-to-Image (S2I), a tool to build artifacts from source repositories to container images.
- Routes - Exposes services to the outside world easily.
- Metrics and logging
- Unified UI

