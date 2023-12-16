package srpr.grpc.twitter;

import java.io.IOException;
import java.util.Properties;

public class Config extends Properties {
    public static final Config CONFIG;
    static {
        CONFIG = new Config();
        try {
            CONFIG.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public String keycloakUrl() {
        return getProperty("keycloak.url");
    }

    public String keycloakClientId() {
        return getProperty("keycloak.client.id");
    }

    public String serverHost() {
        return getProperty("server.host");
    }

    public int serverPort() {
        return Integer.parseInt(getProperty("server.port"));
    }
}
