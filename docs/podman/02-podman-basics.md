# Podman Basic

## Creating Containers with Podman

By default Podman is daemonless.
Podman interacts directly with containers, images, and registries without a daemon.

### Pulling and Displaying Images

Pull

    podman pull registry.redhat.io/rhel7/rhel:7.9

List images

    podman images

    REPOSITORY                        TAG         IMAGE ID      CREATED       SIZE
    registry.redhat.io/rhel7/rhel     7.9         52617ef413bd  4 weeks ago   216 MB

Running and Displaying Containers

    podman run registry.redhat.io/rhel7/rhel:7.9 echo 'Red Hat'

List the running containers:

    podman ps

Show stopped with `--all`

Remove a container when it exits by adding the `--rm` option:

    podman run --rm

name to your containers by adding the `--name`
run in background `-d` 

### Exposing Containers

    podman run -p 8080:8080

Pass environment variables to a container by using the `-e` option

## Networking

default network called `podman`

    podman network create

Creates a new Podman network. This command accepts various options to configure properties of the network, including gateway address, subnet mask

    podman network ls

Lists existing networks

    podman network inspect

Outputs a detailed JSON

    podman network rm

Removes a network.

    podman network prune

Removes any networks that are not currently in use by any running containers.

    podman network connect

Connects an already running container to or from an existing network. 

Alternatively, connect containers to a Podman network on container creation by using the `--net` option. The `disconnect` command disconnects a container from a network.

    podman run -d --name double-connector \
    --net postgres-net,redis-net \
    container-image:latest

Domain name system (DNS) is disabled in the default network

To use DNS, create a new Podman network and connect your containers to that network.

## Accessing Containerized Network Services

### Port Forwarding

Option accepts the form HOST_PORT:CONTAINER_PORT.

    podman run -p 8075:80 my-app

to limit the networks it is accessible from

    podman run -p 127.0.0.1:8075:80 my-app

### List Port Mappings

    podman port my-app

`--all` option lists port mappings for all containers

### Networking in Containers

Containers attached to Podman networks are assigned private IP addresses for each network. Other containers in the network can make requests to this IP address.

retrieves the private IP address of the container

    podman inspect my-app \
    -f '{{.NetworkSettings.Networks.apps.IPAddress}}'

## Accessing containers

### Start Processes in Containers

Start a new process in a running container:

    podman exec [options] container [command ...]

`--env` or `-e` to specify environment variables.
`--interactive` or `-i` to instruct the container to accept input.
`--tty` or `-t` to allocate a pseudo terminal.
`--latest` or `-l` to execute the command in the last created container.

### Copy Files

    podman cp [options] [container]:source_path [container]:destination_path

Without the container name, it points to the local filesystem.

## Container Lifecycle

![lifecycle](./imgs/lifecycle-actions.svg)

### Listing Containers

    podman ps [--all]

### Inspecting Containers

    podman inspect 7763097d11ab

    podman inspect \
    --format='{{.State.Status}}' redhat

### Stopping

    podman stop <name>

    podman stop --all

    podman stop --time=100

    podman kill <name>

### Pausing

    podman pause <id>
    podman unpause <id>

### Restarting

    podman restart <name/id>

### Removing 

    podman rm <id>

`--force` flag to remove the container forcefully.

`--all` flag to remove all stopped containers
