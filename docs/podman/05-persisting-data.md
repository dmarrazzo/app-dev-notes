# Persisting Data

## Volume mounting

Each instruction creates a new read-only data layer. 
Each layer contains a set of changes, or diffs, from the previous layer.

    FROM registry.access.redhat.com/ubi8/ubi-minimal

    RUN microdnf install httpd
    RUN microdnf clean all
    RUN rm -rf /var/cache/yum

    CMD httpd -DFOREGROUND

Show layers:

    podman image tree ubi-httpd

Result:

    Image ID: bdd298c12db7
    Tags:     [localhost/ubi-httpd:latest]
    Size:     166.8MB
    Image Layers
    ├── ID: 647a854c512b Size: 94.77MB
    ├── ID: 3a15f4cd9c23 Size: 20.48kB Top Layer of: [registry.access.redhat.com/ubi8/ubi-minimal:latest]
    ├── ID: d8dcba576e2d Size: 68.45MB
    ├── ID: e9d8a486b7ac Size: 3.581MB
    └── ID: 4c71c24f9911 Size: 4.608kB Top Layer of: [localhost/ubi-httpd:latest]

Union file systems introduce a performance bottleneck for write-intensive containerized processes.

Remove `microdnf` data:

    RUN microdnf install httpd && \
        microdnf clean all && \
        rm -rf /var/cache/yum

### Store Data on Host Machine

- **Persistence**
- **Use of Host File System**:Mounted data typically does not implement the COW file system -> performance
- **Ease of Sharing**

    --volume /path/on/host:/path/in/container:OPTIONS

In the preceding syntax, the :OPTIONS part is optional. Note that you can specify host paths by using absolute paths, such as /home/user/www, or relative paths, such as ./www, which refers to the www directory in the current working directory.

Use `-v` volume_name:/path/in/container to refer to a volume.

Alternatively, you can use the --mount parameter with the following syntax:

    --mount type=TYPE,source=/path/on/host,destination=/path/in/container
T
he --mount parameter explicitly specifies the volume type, such as:

- `bind` for bind mounts.
- `volume` for volume mounts.
- `tmpfs` for creating memory-only, ephemeral mounts.

### Troubleshoot Bind Mounts

    podman run -p 8080:8080 --volume /www:/var/www/html \
    registry.access.redhat.com/ubi8/httpd-24:latest

By default, the Httpd process has insufficient permissions to access the /var/www/html directory

    podman unshare ls -l /www/

To troubleshoot SELinux permission issues, inspect the /www directory SELinux configuration by running the ls command with the -Z option. Use the -d option to print only the directory information.

    [user@host ~]$ ls -Zd /www
    system_u:object_r:default_t:s0:c228,c359 /www

To fix the SELinux configuration, add the :z or :Z option to the bind mount:

- Lower case z lets different containers share access to a bind mount.
- Upper case Z provides the container with exclusive access to the bind mount.

## Volumes commands

    podman volume create http-data

    podman volume inspect http-data

    podman run -p 8080:8080 --volume  http-data:/var/www/html \
        registry.access.redhat.com/ubi8/httpd-24:latest

Because Podman manages the volume, you do not need to configure SELinux permissions.

    podman volume export http_data --output web_data.tar.gz

    podman volume import http_data web_data.tar.gz

Mount is ephemeral but does not use the COW file system:

    podman run -e POSTGRESQL_ADMIN_PASSWORD=redhat --network lab-net \
        --mount  type=tmpfs,tmpfs-size=512M,destination=/var/lib/pgsql/data \
        registry.redhat.io/rhel9/postgresql-13:1

## Working with Databases

Good Practices for Database Containers:
- Use the VOLUME instruction in Containerfiles
- Mount the data directory to a named volume
- Create a database network

Copy the scripts into the container by using the podman cp command:

    podman cp SQL_FILE TARGET_DB_CONTAINER:CONTAINER_PATH

### Import Database Data

Creates an ephemeral container to load data into a PostgreSQL database:

    podman run -it --rm \
        -e PGPASSWORD=DATABASE_PASSWORD \
        -v ./SQL_FILE:/tmp/SQL_FILE:Z \
        --network DATABASE_NETWORK \
        registry.redhat.io/rhel8/postgresql-12:1-113 \
        psql -U DATABASE_USER -h DATABASE_CONTAINER \
            -d DATABASE_NAME -f /tmp/SQL_FILE

### Export Database Data

To export the database data, you can use database backup commands present in the database container image. For example, MySQL provides the mysqldump command and PostgreSQL provides the pg_dump command:

    podman exec POSTGRESQL_CONTAINER \
        pg_dump -Fc DATABASE -f BACKUP_DUMP


