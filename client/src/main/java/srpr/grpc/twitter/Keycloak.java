package srpr.grpc.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public record Keycloak(URI uri, String clientId, String clientSecret) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Keycloak DEFAULT = new Keycloak(
            URI.create(Config.get().keycloakUrl()), Config.get().keycloakClientId(), Config.get().keycloakClientSecret());
    private static final HttpClient httpClient;

    static {
        try {
            httpClient = HttpClient.newBuilder().sslContext(Tls.sslContext()).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Keycloak getDefault() {
        return DEFAULT;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(@JsonProperty("preferred_username") String username, String name, String email) {
    }

    public record Session(BearerTokenCredentials credentials, User user) {
    }

    public Session login(String login, String passwd) throws AuthenticationException {
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
                throw new AuthenticationException(response.body());
            }
            var token = extractToken(response.body());
            return new Session(new BearerTokenCredentials(token), parseTokenPayload(token));
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    public User parseTokenPayload(String token) {
        var chunks = token.split("\\.");
        var decoder = Base64.getUrlDecoder();
        var payload = new String(decoder.decode(chunks[1]));
        try {
            return OBJECT_MAPPER.readValue(payload, User.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractToken(String authResponse) throws IOException {
        var tree = OBJECT_MAPPER.readTree(authResponse);
        return tree.get("access_token").asText();
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
