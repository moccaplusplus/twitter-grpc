package srpr.grpc.twitter;

import java.io.IOException;
import java.util.Properties;

public class Config extends Properties {
    private static final Config CONFIG;
    static {
        CONFIG = new Config();
        try {
            CONFIG.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Config get() {
        return CONFIG;
    }

    public String keycloakUrl() {
        return getProperty("keycloak.url");
    }

    public String keycloakClientId() {
        return getProperty("keycloak.client.id");
    }

    public String keycloakClientSecret() {
        return getProperty("keycloak.client.secret");
    }

    public String serverHost() {
        return getProperty("server.host");
    }

    public int serverPort() {
        return Integer.parseInt(getProperty("server.port"));
    }

    public String keystorePath() {
        return getProperty("keystore.path");
    }

    public String keystorePass() {
        return getProperty("keystore.pass");
    }

    public String truststorePath() {
        return getProperty("truststore.path");
    }

    public String truststorePass() {
        return getProperty("truststore.pass");
    }
}
