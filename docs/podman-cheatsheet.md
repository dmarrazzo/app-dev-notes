# Podman

## Run

```
podman run --rm -it [--pod podname] [--name name] image:tag command
```

`--rm` Remove the container after it exits
`-it` Connect the container to the terminal
`--pod podname` belonging pod
`--name name` Give the container a name
`image:tag` The image used to create the container
`command` A command to run e.g. `/bin/bash`

Other options:

`-e` Set environment variables in container
`-d` Run the container in the background
`-p hostport:containerport` Expose container port
`-v hostdir:containerdir[:Z]` Map the host directory, the optional flag `:Z` disable the security on the local folder 
`--network host` use host network (or other named networks)
`--tmpfs /tmp` ephemeral mount point
`--requires containerName` dependency definition


### Tricks

Run a command with environment variables (in the container scope)

    podman run -it --rm -e TEST="aaa" --entrypoint bash registry.access.redhat.com/ubi8/openjdk-11 -c "echo \$TEST"

Kill all running containers

    podman ps | awk "NR>1 { print \$1 }" | xargs podman kill

## Connect to an existing container

Exec a shell in a running container:

    podman exec -i -t d007c233b329 /bin/bash

Connect to a stopped container, use the image id:

    podman run --rm -it 7fc9591c57a8 /bin/bash

- `-rm` to drop the container at exit

### Explore

Display a live stream of a container’s resource usage

    podman stats container

Return metadata (in JSON) about a running container

    podman inspect container

## Find Images

List all local images

    podman images

Display information about how an image was built

    podman history image:tag

Log in to a remote registry

    podman login registryURL -u username [-p password]

Pull an image from a remote registry

    podman pull registry/username/image:tag

Search local cache and remote registries for images

    podman search searchString

**Note** The list of registries is defined in
`/etc/containers/registries.conf`

Log out of the current remote registry

    podman logout

## Kube

Generate yaml

    podman generate kube my-pod >> my-pod.yaml

Play yaml

    podman play kube ./my-pod.yaml

## Remove

remove / delete

```
podman rm containername
podman rmi imagename
```

Drop all stopped containers:

```
podman rm $(podman ps -q -f status=exited)
```

## Network

    sudo podman network create

## Build Images

Build and tag an image using the instructions in Docker le in the
current directory (don’t forget the dot!)

    podman build -t image:tag .

Same as above, but with a di erent Docker le

    podman build -t image:tag -f Dockerﬁle2

Add an additional name to a local image

    podman tag image:tag image:tag2

Same as above, but the additional name includes a remote registry

    podman tag image:tag registry/username/image:tag

Push an image to a remote registry

    podman push registry/username/image:tag

Create a new image based on the current state of a running
container

    podman commit container newImage:tag

## Other

Create (but don’t start) a container from an image

    podman create [--name name] image:tag

Start an existing container from an image

    podman start container

Restart an existing container

    podman restart container

Wait on one or more containers to stop

    podman wait container1 [container2… ]

Stop a running container gracefully

    podman stop container

Send a signal to a running container

    podman kill container

Remove a container (use -f if the container is running)

    podman rm [-f] container

Remove all containers

    podman container cleanup --all

Podman cleanup

    podman system reset
