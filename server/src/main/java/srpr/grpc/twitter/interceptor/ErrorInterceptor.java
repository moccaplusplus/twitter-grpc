package srpr.grpc.twitter.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import static io.grpc.Status.INTERNAL;

@Component
public class ErrorInterceptor implements ServerInterceptor {
    @Override
    public <T, R> ServerCall.Listener<T> interceptCall(ServerCall<T, R> call, Metadata headers, ServerCallHandler<T, R> next) {
        try {
            return next.startCall(call, headers);
        } catch (StatusRuntimeException e) {
            throw e;
        } catch (RuntimeException e) {
            var description = e.getClass().getSimpleName() + " " + e.getMessage();
            var status = INTERNAL.withDescription(description);
            throw new StatusRuntimeException(status);
        }
    }
}
