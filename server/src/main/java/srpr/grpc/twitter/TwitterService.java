package srpr.grpc.twitter;

import com.google.rpc.Code;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import srpr.grpc.twitter.TwitterServiceGrpc.TwitterServiceImplBase;
import srpr.grpc.twitter.TwitterServiceOuterClass.Twit;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterService extends TwitterServiceImplBase {
    private final StorageService storageService;

    @Override
    public void addTwit(Twit request, StreamObserver<TwitItem> responseObserver) {
        try {
            log.info("Received a new twit to add: {}", request);
            var response = storageService.storeTwit(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in addTwit method with request payload: " + request, e);
            responseObserver.onError(toError(e));
        }
    }

    @Override
    public void getTwits(TwitterServiceOuterClass.TwitGetRequest request, StreamObserver<TwitItem> responseObserver) {
        try {
            log.info("Received request for delivering twits: {}", request);
            var count = request.getCount();
            var twits = storageService.getLastTwits(count);
            twits.forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getTwits method with request payload: " + request, e);
            responseObserver.onError(toError(e));
        }
    }

    private static StatusRuntimeException toError(Exception e) {
        var status = com.google.rpc.Status.newBuilder()
                .setCode(Code.INTERNAL.getNumber())
                .setMessage(String.join(" ", e.getClass().getSimpleName(), e.getMessage()))
                .build();
        return StatusProto.toStatusRuntimeException(status);
    }
}
