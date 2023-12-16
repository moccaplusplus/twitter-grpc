package srpr.grpc.twitter;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.Status;
import io.grpc.StatusException;

import java.util.concurrent.Executor;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.netty.handler.codec.http.HttpHeaderNames.AUTHORIZATION;

public class BearerTokenCredentials extends CallCredentials {
    public static final Key<String> METADATA_KEY = Key.of(AUTHORIZATION.toString(), ASCII_STRING_MARSHALLER);
    public static final String PREFIX = "Bearer ";

    public static String extractToken(Metadata headers) {
        var authHeader = headers.get(METADATA_KEY);
        if (authHeader == null) {
            throw new NullPointerException("Authorization header is missing");
        }
        if (!authHeader.startsWith(PREFIX)) {
            throw new IllegalStateException("Expected to start with prefix " + PREFIX);
        }
        return authHeader.substring(PREFIX.length());
    }

    private final String token;

    public BearerTokenCredentials(String token) {
        this.token = token;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier metadataApplier) {
        executor.execute(() -> {
            try {
                var headers = new Metadata();
                headers.put(METADATA_KEY, PREFIX + token);
                metadataApplier.apply(headers);
            } catch (Throwable e) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }
}
