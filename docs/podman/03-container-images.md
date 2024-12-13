# Container images

## Container file

    FROM registry.redhat.io/ubi9
    CMD echo "Hello world"

## Pull Images

    podman pull registry.redhat.io/ubi8/ubi:8.6

## Registry

Registry:

- no authentication: `registry.access.redhat.com`
- authentication: `registry.redhat.io`

Web catalog https://catalog.redhat.com/

Shorter, unqualified name, which avoids the registry URL:

    podman pull ubi8/python-39

Which registry to use: `/etc/containers/registries.conf`

Example of how to block a registry:

    location="docker.io"
    blocked=true

On Microsoft Windows, execute `podman machine ssh`

Registry Credentials:

    podman login registry.redhat.io

Podman stores the credentials in the `${XDG_RUNTIME_DIR}/containers/auth.json`

## Managing Images

Image versions can be used in the image name or in the **image tag**. An image tag is just a string that you specify after the image name. Also, the same image can have multiple tags.

    [<image repository>/<namespace>/]<image name>[:<tag>]

Tags are optional, Podman uses the `latest` tag by default.

New tag:

    podman image tag LOCAL_IMAGE:TAG LOCAL_IMAGE:NEW_TAG

Search image:

    podman search nginx

List:

    podman image ls

Build:

    podman build --file CONTAINERFILE --tag IMAGE_REFERENCE

Push:

    podman push quay.io/YOUR_QUAY_USER/IMAGE_NAME:TAG

Inspect:

    podman image inspect registry.redhat.io/rhel8/mariadb-103:1

    podman image tree registry.redhat.io/rhel8/mariadb-103:1

Removal:

    podman image rm REGISTRY/NAMESPACE/IMAGE_NAME:TAG
    podman rmi --all # all images
    podman image rm --all

Images without tags and that are not referenced by other images are considered **dangling images**.

    podman image prune

Remove all images without containers:

    podman image prune -a



