package srpr.grpc.twitter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Context;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import srpr.grpc.twitter.document.UserDoc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final Context.Key<UserDoc> USER = Context.key("user");

    private final StorageService storageService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.key-url}")
    private String keycloakKeyUrl;
    private JwtParser parser;

    @PostConstruct
    public void postConstruct() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InterruptedException {
        var keycloakKey = getKeycloakKey();
        var keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(keycloakKey));
        var keyFactory = KeyFactory.getInstance("RSA");
        var publicKey = keyFactory.generatePublic(keySpec);
        parser = Jwts.parser().verifyWith(publicKey).build();
    }

    public UserDoc getLoggedUser() {
        return USER.get(Context.current());
    }

    public Context initSession(String token) {
        var user = parseToken(token);
        user = storageService.saveUser(user);
        return Context.current().withValue(USER, user);
    }

    private String getKeycloakKey() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(keycloakKeyUrl))
                .header(ACCEPT.toString(), APPLICATION_JSON.toString())
                .GET().build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        var tree = objectMapper.readTree(response.body());
        return tree.get("public_key").asText();
    }

    private UserDoc parseToken(String token) {
        var claims = parser.parseSignedClaims(token);
        var payload = claims.getPayload();
        return UserDoc.builder()
                .name(payload.get("preferred_username", String.class))
                .givenName(payload.get("given_name", String.class))
                .familyName(payload.get("family_name", String.class))
                .email(payload.get("email", String.class))
                .build();
    }
}
