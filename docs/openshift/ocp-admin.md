# OCP Administration

## Add Identity Provider (IDP)

https://docs.openshift.com/container-platform/4.14/authentication/identity_providers/configuring-htpasswd-identity-provider.html

Create htpasswd and first user:

    htpasswd -c -B -b users.htpasswd <username> <password>

Add user:

    htpasswd -B -b users.htpasswd <user_name> <password>

Delete user:

    htpasswd -D users.htpasswd <username>


Create a secret

    oc create secret generic htpass-secret --from-file=htpasswd=users.htpasswd -n openshift-config

Create IDP

    echo "
    apiVersion: config.openshift.io/v1
    kind: OAuth
    metadata:
      name: cluster
    spec:
      identityProviders:
      - name: my_htpasswd_provider 
        mappingMethod: claim 
        type: HTPasswd
        htpasswd:
          fileData:
            name: htpass-secret" | oc apply -f -


Replace:

    oc create secret generic htpass-secret --from-file=htpasswd=users.htpasswd --dry-run=client -o yaml -n openshift-config | oc replace -f -

Grant privileges:

```sh
oc adm policy add-cluster-role-to-user cluster-admin admin --rolebinding-name=cluster-admin
```

Show current users  with cluster-admin role:

```sh
oc describe clusterrolebindings cluster-admin
```
