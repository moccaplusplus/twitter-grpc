package srpr.grpc.twitter.interceptor;

import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import srpr.grpc.twitter.BearerTokenCredentials;
import srpr.grpc.twitter.service.SessionService;

import static io.grpc.Status.UNAUTHENTICATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthInterceptor implements ServerInterceptor {
    private final SessionService sessionService;

    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(ServerCall<T, R> serverCall, Metadata metadata, ServerCallHandler<T, R> next) {
        try {
            var token = BearerTokenCredentials.extractToken(metadata);
            var ctx = sessionService.initSession(token);
            return Contexts.interceptCall(ctx, serverCall, metadata, next);
        } catch (Exception e) {
            var status = UNAUTHENTICATED.withDescription(e.getMessage());
            serverCall.close(status, metadata);
            throw new StatusRuntimeException(status);
        }
    }
}
