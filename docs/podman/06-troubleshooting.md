# Troubleshooting

list all containers:

    podman ps -a

Logs:

    podman logs CONTAINER

Port issues:

    podman port CONTAINER

Show tcp statistics:

    ss -pant

in containers

    podman exec -it CONTAINER ss -pant

If you lack a tool in the container, you can leverage the `nsenter` tool from host

    sudo nsenter -n -t CONTAINER_PID ss -pant

NOTE: Containerized applications should listen on the 0.0.0.0 address

Other networking issues:

    podman inspect CONTAINER --format='{{.NetworkSettings.Networks}}'

    podman network inspect NETWORK

NOTE: default network cannot resolve host names (`"dns_enabled": true`)
