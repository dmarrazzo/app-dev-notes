# Custom Container Images

the base image you choose determines the Linux distribution, as well as any or all of the following components:

- Package manager
- Init system
- Filesystem layout
- Preinstalled dependencies and runtimes

UBIs use Red Hat Enterprise Linux (RHEL):

- **Standard** primary UBI which includes DNF, systemd, and utilities such as gzip and tar.
- **Init** Eases running multiple applications within a single container by managing them with systemd.
- **Minimal** Smaller than the init image and still provides nice-to-have features. This image uses the microdnf minimal package manager instead of the full-sized version of DNF.
- **Micro** This is the smallest available UBI because it only includes the bare minimum number of packages. For example, this image does not include a package manager.

## Containerfile Instructions

`FROM` Takes the name of the base image as an argument.

`WORKDIR` Sets the current working directory within the container. Later instructions run within this directory.

`COPY` Copies files from the build host into the file system of the resulting container image. 

> NOTE: `--chown=user` option help you to copy and change ownership in one step

`ADD` Copies files or folders from a local or remote source and adds them to the container’s file system. If used to copy local files, those must be in the working directory. The ADD instruction also unpacks local .tar archive files to the destination image directory.

`RUN` Runs a command in the container and commits the resulting state of the container to a new layer within the image.

`ENTRYPOINT` Sets the executable to run when the container is started.

`CMD` Runs a command when the container is started. This command is passed to the executable defined by ENTRYPOINT. Base images define a default ENTRYPOINT, which is usually a shell executable, such as Bash.

NOTE: Neither ENTRYPOINT nor CMD run while building a container image. They are executed within a container as it is initialized.

`USER` Changes the active user within the container. Later instructions run as this user, including the CMD instruction. It is a good practice to define a different user other than root for security reasons.

`LABEL` Adds a key-value pair to the metadata of the image for organization and image selection.

`EXPOSE` Adds a port to the metadata for the image indicating that an application within the container will bind to this port. Note that this does not bind the port on the host and is for documentation purposes.

`ENV` Defines environment variables that are available in the container. You can declare multiple ENV instructions within the Containerfile. You can use the env command inside the container to view each of the environment variables.

`ARG` Defines build-time variables, typically to make a customizable container build. Developers commonly configure the ENV instructions by using the ARG instruction. This is useful for preserving the build-time variables for run time.

`VOLUME` Defines where to store data outside of the container. The value designates the path where Podman mounts the directory inside of the container. You can define more than one path to create multiple volumes.

## Advanced Containerfile Instructions

### ARG

You can configure a default build-time variable value if the developer does not provide it at build time. Use the following syntax to define a build-time variable:

    ARG key[=default value]

Example:

    ARG VERSION="1.16.8" \
        BIN_DIR=/usr/local/bin/

    RUN curl "https://dl.example.io/${VERSION}/example-linux-amd64" \
            -o ${BIN_DIR}/example

Pass arguments:

    podman --build-arg VERSION=1.16.9

### VOLUME

Stores data outside of the container

    FROM registry.redhat.io/rhel9/postgresql-13:1

    VOLUME /var/lib/pgsql/data

Create a volume not bound to the container lifecycle:

    podman volume create VOLUME_NAME

List volumes:

    podman volume ls \
     --format="{{.Name}}\t{{.Mountpoint}}"

### ENTRYPOINT and CMD

Specify the command to execute when the container starts

    FROM registry.access.redhat.com/ubi8/ubi-minimal:8.5
    ENTRYPOINT ["echo", "Hello"]

Running container:

    podman run my-image
    Hello

Passing further arguments:

    podman run my-image Red Hat
    Hello Red Hat

CMD does not concatenate podman arguments:

    FROM registry.access.redhat.com/ubi8/ubi-minimal:8.5
    CMD ["echo", "Hello", "Red Hat"]

So if you run it:

    podman run my-image Red Hat
    Error: crun: executable file `Red` not found in $PATH: No such file or directory: OCI runtime attempted to invoke a command that was not found

When a Containerfile specifies both ENTRYPOINT and CMD then CMD changes its behavior. In this case the values provided to CMD are passed as default arguments to the ENTRYPOINT.

    FROM registry.access.redhat.com/ubi8/ubi-minimal:8.5
    ENTRYPOINT ["echo", "Hello"]
    CMD ["Red", "Hat"]

## Multistage build

    # First stage
    FROM registry.access.redhat.com/ubi8/nodejs-14:1 as builder
    COPY ./ /opt/app-root/src/
    RUN npm install
    RUN npm run build

    # Second stage
    FROM registry.access.redhat.com/ubi8/nodejs-14-minimal:1
    COPY --from=builder /opt/app-root/src/ /opt/app-root/src/
    EXPOSE 8080
    CMD node dist/index.js

## Examine Container Data Layers

### Cache Image Layers

    ...content omitted...
    COPY package.json /app/
    RUN  RUN npm ci --production
    COPY src ./src
    ...content omitted...

if you change the source folder content, the layers before COPY are recycled (from cache)

### Reduce Image Layers

    RUN mkdir /etc/gitea && \
        chown root:gitea /etc/gitea && \
        chmod 770 /etc/gitea

The advantage of chaining commands is that you create less container image layers, which typically results in **smaller** images.

You can also create Containerfiles that do not use chained commands, and configure Podman to squash the layers:

- `--squash` option to squash layers
- `--squash-all` option to also squash the layers from the parent image.

## Rootless Podman

Change container user:

    FROM registry.access.redhat.com/ubi9/ubi

    RUN adduser \
    --no-create-home \
    --system \
    --shell /usr/sbin/nologin \
    python-server

    USER python-server

    CMD ["python3", "-m", "http.server"]

User Mapping:

    cat /etc/subuid /etc/subgid
    student:100000:65536
    student:100000:65536

the student user can allocate 65536 user IDs starting with the ID 100000.

Check mapping with:

    podman top e6116477c5c9 huser user

If you run podman with root priviledges, the container user will be mapped on root
