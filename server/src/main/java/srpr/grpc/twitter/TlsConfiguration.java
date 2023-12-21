package srpr.grpc.twitter;

import io.grpc.ServerCredentials;
import io.grpc.TlsServerCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Configuration
public class TlsConfiguration {
    private static final String KEYSTORE_TYPE = "PKCS12";
//    private static final String KEYSTORE_TYPE = "JKS";
    @Value("${keystore.path}")
    private String keystorePath;
    @Value("${keystore.pass}")
    private String keystorePass;
    @Value("${truststore.path}")
    private String truststorePath;
    @Value("${truststore.pass}")
    private String truststorePass;

    @Bean
    public ServerCredentials tlsCredentials() throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, UnrecoverableKeyException {
        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(loadKeyStore(truststorePath, truststorePass));
        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(loadKeyStore(keystorePath, keystorePass), keystorePass.toCharArray());
        return TlsServerCredentials.newBuilder()
                .trustManager(trustManagerFactory.getTrustManagers())
                .keyManager(keyManagerFactory.getKeyManagers())
                .build();
    }

    private KeyStore loadKeyStore(String path, String pass) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        var keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            keyStore.load(is, pass.toCharArray());
        }
        return keyStore;
    }
}
