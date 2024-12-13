# Create the VM

Customize adding the public key (secret)

Before you have to deploy the secret:

```sh
oc create secret generic donato-rsa-pub --from-file=.ssh/id_rsa.pub -n debezium
```

## Enable the external port

Create the service with the following `yaml`, make sure to match the VM label `kubevirt.io/domain`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: connect-ssh
  namespace: debezium
spec:
  type: NodePort
  externalTrafficPolicy: Cluster
  ports:
  - port: 22
    protocol: TCP
  selector:
    kubevirt.io/domain: rhel8-connect
```