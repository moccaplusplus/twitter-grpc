package srpr.grpc.twitter;

import io.grpc.CallCredentials;
import io.grpc.internal.JsonParser;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static srpr.grpc.twitter.Config.CONFIG;

public record Keycloak(URI uri, String clientId, String clientSecret) {
    public static final Keycloak DEFAULT = new Keycloak(
            URI.create(CONFIG.keycloakUrl()), CONFIG.keycloakClientId(), CONFIG.keycloakClientSecret());
    private static final HttpClient httpClient;

    static {
        try {
            httpClient = HttpClient.newBuilder().sslContext(Tls.sslContext()).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public CallCredentials login(String login, String passwd) throws AuthenticationException {
        var params = new HashMap<String, String>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("grant_type", "password");
        params.put("username", login);
        params.put("password", passwd);

        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header(CONTENT_TYPE.toString(), APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .header(ACCEPT.toString(), APPLICATION_JSON.toString())
                .POST(toPostBody(params))
                .build();
        try {
            var response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new AuthenticationException("Fail: " + response.body());
            }
            return new BearerTokenCredentials(extractToken(response.body()));
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private static String extractToken(String authResponse) throws IOException {
        var json = (Map) JsonParser.parse(authResponse);
        return (String) json.get("access_token");
    }

    private static BodyPublisher toPostBody(Map<String, String> params) {
        return BodyPublishers.ofString(toUrlEncodedString(params));
    }

    private static String toUrlEncodedString(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> Stream.of(e.getKey(), e.getValue())
                        .map(s -> URLEncoder.encode(s, UTF_8))
                        .collect(joining("=")))
                .collect(joining("&"));
    }
}
