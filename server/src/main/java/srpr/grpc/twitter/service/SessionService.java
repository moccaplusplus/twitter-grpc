package srpr.grpc.twitter.service;

import io.grpc.Context;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import srpr.grpc.twitter.document.UserDoc;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final Context.Key<UserDoc> USER = Context.key("user");

    private final StorageService storageService;

    @Value("${keycloak.key}")
    private String keycloakKey;
    private JwtParser parser;

    @PostConstruct
    public void postConstruct() throws NoSuchAlgorithmException, InvalidKeySpecException {
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
