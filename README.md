# twitter-grpc

An implementation of a "mini-twitter" with use of gRPC. Just a showcase.
Basic working version done.

## TLS

CA and trust store.

```shell
# CA keystore
keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore ca.jks -validity 365 -dname "CN=CA, OU=CA, O=Twitter gRPC" -ext KeyUsage=digitalSignature,keyCertSign -ext BasicConstraints=ca:true,PathLen:3 -storepass twitter -keypass twitter
keytool -exportcert -alias ca -keystore ca.jks -file ca.pem -rfc -storepass twitter

# Trust store
keytool -importcert -alias ca -keystore trust.jks -file ca.pem -noprompt -storepass trustpass
```

gRPC - mutual TLS.

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

Keycloak keystore. (Remember to add `ca.pem` to browser trusted root CAs.)

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