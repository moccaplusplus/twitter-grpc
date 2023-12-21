package srpr.grpc.twitter;

import io.grpc.ChannelCredentials;
import io.grpc.TlsChannelCredentials;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static srpr.grpc.twitter.Config.CONFIG;

public class Tls {
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final TrustManagerFactory TRUST_MANAGER_FACTORY;
    private static final KeyManagerFactory KEY_MANAGER_FACTORY;

    static {
        try {
            TRUST_MANAGER_FACTORY = trustManagerFactory();
            KEY_MANAGER_FACTORY = keyManagerFactory();
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException |
                 UnrecoverableKeyException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static ChannelCredentials channelCredentials() {
        return TlsChannelCredentials.newBuilder()
                .trustManager(TRUST_MANAGER_FACTORY.getTrustManagers())
                .keyManager(KEY_MANAGER_FACTORY.getKeyManagers())
                .build();
    }

    public static SSLContext sslContext() throws NoSuchAlgorithmException, KeyManagementException {
        var sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, TRUST_MANAGER_FACTORY.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    private static TrustManagerFactory trustManagerFactory() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException {
        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(loadKeyStoreFromResources(CONFIG.truststorePath(), CONFIG.truststorePass()));
        return trustManagerFactory;
    }

    private static KeyManagerFactory keyManagerFactory() throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, UnrecoverableKeyException {
        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(
                loadKeyStoreFromResources(CONFIG.keystorePath(), CONFIG.keystorePass()),
                CONFIG.keystorePass().toCharArray());
        return keyManagerFactory;
    }

    private static KeyStore loadKeyStoreFromResources(String path, String pass) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        var keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        try (var is = Tls.class.getClassLoader().getResourceAsStream(path)) {
            keyStore.load(is, pass.toCharArray());
        }
        return keyStore;
    }
}
