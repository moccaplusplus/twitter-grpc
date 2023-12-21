package srpr.grpc.twitter;

import io.grpc.Grpc;
import io.grpc.Server;
import io.grpc.ServerCredentials;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import srpr.grpc.twitter.interceptor.AuthInterceptor;
import srpr.grpc.twitter.interceptor.ErrorInterceptor;
import srpr.grpc.twitter.service.TwitterService;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrpcServerRunner implements CommandLineRunner {
    private final AuthInterceptor authInterceptor;
    private final ErrorInterceptor errorInterceptor;
    private final TwitterService twitterService;
    private final ServerCredentials tlsCredentials;
    @Value("${grpc.server.port}")
    private int serverPort;
    private Server server;

    @Override
    public void run(String... args) throws IOException, InterruptedException {
        log.info("Starting gRPC Twitter Server...");
        server = Grpc.newServerBuilderForPort(serverPort, tlsCredentials)
                .executor(ForkJoinPool.commonPool())
                .intercept(authInterceptor)
                .intercept(errorInterceptor)
                .addService(twitterService).build();
        server.start();
        log.info("gRPC Twitter Server running port {}", serverPort);
        server.awaitTermination();
        log.info("gRPC Twitter Server closed");
    }

    @PreDestroy
    public void preDestroy() {
        server.shutdown();
    }
}