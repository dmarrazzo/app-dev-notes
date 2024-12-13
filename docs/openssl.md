# OpenSSL cheat sheet

Based on 

- https://mariadb.com/docs/security/data-in-transit-encryption/create-self-signed-certificates-keys-openssl/
- https://www.golinuxcloud.com/add-x509-extensions-to-certificate-openssl/

## Creating the Certificate Authority's Certificate and Keys

Generate a private key for the CA:

```sh
openssl genrsa 2048 > ca-key.pem
```

Generate the X509 certificate for the CA:

```sh
openssl req -new -x509 -nodes -days 365000 \
    -key ca-key.pem \
    -out ca-cert.pem
```

## Creating the Server's Certificate and Keys

Generate the private key and certificate request:

```sh
openssl req -newkey rsa:2048 -nodes -days 365000 \
    -keyout server-key.pem \
    -out server-req.pem
```

Generate the X509 certificate for the server:

```sh
openssl x509 -req -days 365000 -set_serial 01 \
   -in server-req.pem \
   -out server-cert.pem \
   -CA ca-cert.pem \
   -CAkey ca-key.pem
```

Adding **Subject Alternative Names** (SANs):

Create the config file:

```
[ req ]
default_bits       = 2048
distinguished_name = req_distinguished_name
req_extensions     = req_ext

[ req_distinguished_name ]
countryName            = Country Name (2 letter code)
stateOrProvinceName    = State or Province Name (full name)
localityName           = Locality Name (eg, city)
organizationName       = Organization Name (eg, company)
commonName             = Common Name (e.g. server FQDN or YOUR name)

# default 
countryName_default            = IT
stateOrProvinceName_default    = RM
localityName_default           = RM
0.organizationName_default     = DMShift
organizationalUnitName_default = kafka
commonName_default             = dmshift.eu

[ req_ext ]
subjectAltName = @alt_names

[ alt_names ]
DNS.1   = dmshift.eu
DNS.2   = *.dmshift.eu
DNS.3   = my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu
DNS.4   = my-cluster-kafka-tls-0-kafka.apps.ocp4.dmshift.eu
DNS.5   = my-cluster-kafka-tls-1-kafka.apps.ocp4.dmshift.eu
DNS.6   = my-cluster-kafka-tls-2-kafka.apps.ocp4.dmshift.eu
```

Generate the private key and certificate request:

```sh
openssl req -newkey rsa:2048 -nodes \
   -keyout server-key.pem \
   -out server-req.pem -config san.cnf -reqexts req_ext
```

Inspect the request:

```sh
openssl req -text -in server-req.pem
```

Generate the X509 certificate for the server:

```sh
openssl x509 -req -days 365 \
   -in server-req.pem \
   -out server-cert.pem \
   -CA ca-cert.pem \
   -CAkey ca-key.pem \
-CAcreateserial -extensions req_ext -extfile san.cnf
```

Inspect the certificate

```sh
openssl x509 -text -noout -in server-cert.pem
```

## Creating the Client's Certificate and Keys

Generate the private key and certificate request:

```sh
openssl req -newkey rsa:2048 -nodes -days 365000 \
   -keyout client-key.pem \
   -out client-req.pem
```

Generate the X509 certificate for the client:

```sh
openssl x509 -req -days 365000 -set_serial 01 \
   -in client-req.pem \
   -out client-cert.pem \
   -CA ca-cert.pem \
   -CAkey ca-key.pem
```

## Verifying the Certificates

Verify the server certificate:

```sh
openssl verify -CAfile ca-cert.pem \
   server-cert.pem
```

Verify the client certificate:

```sh
openssl verify -CAfile ca-cert.pem \
   client-cert.pem
```

connect to remote server:

```
openssl s_client -CAfile ca-cert.pem my-cluster-kafka-tls-bootstrap-kafka.apps.ocp4.dmshift.eu:443
```