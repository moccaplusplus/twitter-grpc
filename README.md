# twitter-grpc

A showcase application for using `gRPC` in Java.

This is an implementation of a "mini-twitter" with use of `gRPC`.

### Requirements

- JDK 18 (or newer) installed.

  Remember to set up `JAVA_HOME` env var
  and to update `PATH` env var to point to `${JAVA_HOME}/bin`.

- Docker installed.

- **Internet connection** - maven downloads all declared dependencies during first build.

## Building application

In main directory of this project type:

```shell
./mvnw clean install
```

## Running application

### Server

Enter `server` directory.

```shell
cd ./server
```

To start the environment needed by server, type in console:

```shell
docker compose up
```

After the all services in docker are up, in order to star server application, type (in the same directory):

```shell
java -jar ./target/server-1.0-SNAPSHOT.jar
```

### Client

In order to start client application enter `client` directory:

```shell
cd ../client # assuming we are in server - not in project's root
```

Then type:

```shell
java -jar ./target/client-1.0-SNAPSHOT.jar
```

In order to login into client app use the following credentials:

- user: `twitter`, passwd: `twitter`

More information on user's is provided in section Architecture, subsection Keycloak.

## Architecture

Project is divided into 3 submodules:

- Client
- Lib
- Server

### Lib

Contains code used both by server and client. Mainly java code generated from `.proto` definitions.

### Client

Client module contains source code for simple client GUI application.
Client module is written using JavaFX.

### Server

Contains server code implementing services defined by `.proto` definitions.
Server project is written using Spring Boot framework. It also relies on some external services which are described
below.

All external services needed by the server application are provisioned with use of docker.
After docker compose is up the following services should be available:

- Keycloak, running on https://localhost:8443.
- Mongo DB, running on localhost:27017
- Mongo Express (web interface to Mongo) running on http://localhost:8081

### Keycloak

Keycloak is used for user management.
The project shows how to use Bearer Token Credentials together with TLS Channel Credentials in gRCP.

In order to login into Keycloak management console on https://localhost:8443 use the following credentials:

- user: `admin`, passwd: `admin`

This user resides in `master` realm and is used only to login into Keycloak management console, it will not be able to
log into client application.

Client application uses users in realm `twitter`. There are 3 users added in this realm.

- user: `twitter`, passwd: `twitter`
- user: `user1`, passwd: `user1`
- user: `user2`, passwd: `user2`

When starting environment with docker compose all the above users should be available in Keycloak. Use any of them in
the client application.

A command for exporting realm from keycloak.

```shell
# connect to running container's shell
docker exec -it server-keycloak-1 bash

# in container's shell
cd /opt/keycloak
bin/kc.sh export --file twitter-realm.json --users realm_file --realm twitter
exit

# in local filesystem
docker cp server-keycloak-1:/opt/keycloak/twitter-realm.json ./
```

You do not need to use it unless you want save your changes in keycloak realm.

### Mongo

Mongo DB is used for data persistence. It runs on port 27017. You do not need to connect to it directly, the server app
does. There are two users in registered in Mongo:

- user: `mongo`, passwd: `mongo`, admin user for full access
- user: `mongo`, passwd: `mongo`, twitter user for limited access to twitter database only

Exporting mongo data
```shell
docker exec -it server-mongo-1 bash

# in container's shell
mongoexport --authenticationDatabase admin --uri mongodb://mongo:mongo@localhost:27017 --db twitter --collection user --type=json --out "/data/db/user.json" --jsonArray
mongoexport --authenticationDatabase admin --uri mongodb://mongo:mongo@localhost:27017 --db twitter --collection twit --type=json --out "/data/db/twit.json" --jsonArray
exit

# in local filesystem
docker cp server-mongo-1:/data/db/user.json ./
docker cp server-mongo-1:/data/db/twit.json ./
```
You do not need to use it unless you want save your added twits.

### Mongo Express

Mongo express is a web interface for Mongo DB. Use the following credentials to login:

- user: `mongo`, passwd: `mongo`

To view the existing twit database select `twitter` database in web ui.

## TLS

This section describes how TLS has been set up. No need to repeat these steps - they have already been done. This is
only for informative purpose.

### CA and trust store.

```shell
# CA keystore
keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore ca.jks -validity 365 -dname "CN=CA, OU=CA, O=Twitter gRPC" -ext KeyUsage=digitalSignature,keyCertSign -ext BasicConstraints=ca:true,PathLen:3 -storepass twitter -keypass twitter
keytool -exportcert -alias ca -keystore ca.jks -file ca.pem -rfc -storepass twitter

# Trust store
keytool -importcert -alias ca -keystore trust.jks -file ca.pem -noprompt -storepass trustpass
```

### gRPC - mutual TLS.

```shell
# Server keystore
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore server.jks -validity 365 -dname "CN=Server, OU=Server, O=Twitter gRPC" -ext KeyUsage=digitalSignature,keyEncipherment -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:127.0.0.1 -storepass serverpass -keypass serverpass
keytool -importcert -alias ca -keystore server.jks -file ca.pem -noprompt -storepass serverpass
keytool -certreq -keystore server.jks -alias server -file server.csr -keyalg RSA -keypass serverpass -storepass serverpass
keytool -gencert -alias ca -keystore ca.jks -infile server.csr -outfile server.cer -validity 365 -ext KeyUsage=digitalSignature,keyEncipherment -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:127.0.0.1 -rfc -storepass twitter
keytool -importcert -alias server -keystore server.jks -file server.cer -storepass serverpass
keytool -delete -alias ca -keystore server.jks -storepass serverpass
rm server.csr 
rm server.cer
mv server.jks ./server/src/main/resources/
cp trust.jks ./server/src/main/resources/

# Client keystore
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore client.jks -validity 365 -dname "CN=Client, OU=Client, O=Twitter gRPC" -ext KeyUsage=digitalSignature,keyEncipherment -ext ExtendedKeyUsage=serverAuth,clientAuth -storepass clientpass -keypass clientpass
keytool -importcert -alias ca -keystore client.jks -file ca.pem -noprompt -storepass clientpass
keytool -certreq -keystore client.jks -alias client -file client.csr -keyalg RSA -keypass clientpass -storepass clientpass
keytool -gencert -alias ca -keystore ca.jks -infile client.csr -outfile client.cer -validity 365 -ext KeyUsage=digitalSignature,keyEncipherment -ext ExtendedKeyUsage=serverAuth,clientAuth -rfc -storepass twitter
keytool -importcert -alias client -keystore client.jks -file client.cer -storepass clientpass
keytool -delete -alias ca -keystore client.jks -storepass clientpass
rm client.csr 
rm client.cer
mv client.jks ./client/src/main/resources/
cp trust.jks ./client/src/main/resources/
```

### Keycloak keystore.

```shell
keytool -genkeypair -alias keycloak -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keycloak.jks -validity 365 -dname "CN=Auth, OU=Auth, O=Twitter gRPC" -ext KeyUsage=digitalSignature,keyEncipherment -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:127.0.0.1 -storepass keycloakpass -keypass keycloakpass
keytool -importcert -alias ca -keystore keycloak.jks -file ca.pem -noprompt -storepass keycloakpass
keytool -certreq -keystore keycloak.jks -alias keycloak -file keycloak.csr -keyalg RSA -keypass keycloakpass -storepass keycloakpass
keytool -gencert -alias ca -keystore ca.jks -infile keycloak.csr -outfile keycloak.cer -validity 365 -ext KeyUsage=digitalSignature,keyEncipherment -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:127.0.0.1 -rfc -storepass twitter
keytool -importcert -alias keycloak -keystore keycloak.jks -file keycloak.cer -storepass keycloakpass
keytool -delete -alias ca -keystore keycloak.jks -storepass keycloakpass
rm keycloak.csr 
rm keycloak.cer
mv keycloak.jks ./server/
```

Remember to add `ca.pem` to browser's trusted root CAs or accept the untrusted certificate when prompted by the browser.