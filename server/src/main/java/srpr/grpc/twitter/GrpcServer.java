package srpr.grpc.twitter;

import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrpcServer implements CommandLineRunner {
    private final TwitterService twitterService;

    @Value("${grpc.server.port}")
    private int port;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting gRPC Twitter Server on port {}", port);
        var server = ServerBuilder
                .forPort(port)
                .executor(ForkJoinPool.commonPool())
                .addService(twitterService).build();
        server.start();
        log.info("gRPC Twitter Server running port {}", port);
        server.awaitTermination();
    }
}
