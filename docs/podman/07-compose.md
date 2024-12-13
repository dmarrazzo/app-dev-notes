# Multi-container Applications with Compose

Compose file:

```yaml
services:
orders:
    image: quay.io/user/python-app
    ports:
    - "3030:8080"
    environment:
      ACCOUNTS_SERVICE: http://accounts
```
- version (deprecated): Specifies the Compose version used.
- services: Defines the containers used.
- networks: Defines the networks used by the containers.
- volumes: Specifies the volumes used by the containers.
- configs: Specifies the configurations used by the containers.
- secrets: Defines the secrets used by the containers.

Start: 
    podman-compose up

Stop:
    podman-compose down

Podman generates predictable names for objects that are not explicitly named. In the preceding example, Podman creates a default network for the application.

The default network naming convention uses the current directory name and the _default suffix. The preceding example contains the network user_default because it shows a Compose file executed in the /home/user home directory.

`-d`, --detach: Start containers in the background.
`--force-recreate`: Re-create containers on start.
`-V`, --renew-anon-volumes: Re-create anonymous volumes.
`--remove-orphans`: Remove containers that do not correspond to services that are defined in the current Compose file.


`networks`create and use Podman networks.

If the networks keyword is not defined in the Podman Compose file, then Podman Compose creates a default DNS-enabled network.

```yaml
services:
  frontend:
    image: quay.io/example/frontend
    networks:
      - app-net
    ports:
      - "8082:8080"
  backend:
    image: quay.io/example/backend
    networks:
      - app-net
      - db-net
  db:
    image: registry.redhat.io/rhel8/postgresql-13
    environment:
      POSTGRESQL_ADMIN_PASSWORD: redhat
    networks:
      - db-net

networks:
  app-net: {}
  db-net: {}
```

Volumes:


```yaml
services:
  db:
    image: registry.redhat.io/rhel8/postgresql-13
    environment:
      POSTGRESQL_ADMIN_PASSWORD: redhat
    ports:
      - "5432:5432"
    volumes:
      - db-vol:/var/lib/postgresql/data

volumes:
  db-vol: {}
```

If you created a volume that is not managed by Podman Compose:

```yaml
volumes:
  my-volume:
    external: true
```


```yaml
    volumes:
      - ./local/redhat:/var/lib/postgresql/data:Z
```






