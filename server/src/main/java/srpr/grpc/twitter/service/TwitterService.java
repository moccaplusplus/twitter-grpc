package srpr.grpc.twitter.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import srpr.grpc.twitter.TwitterServiceGrpc.TwitterServiceImplBase;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitAddRequest;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitGetRequest;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitGetResponse;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwitterService extends TwitterServiceImplBase {
    private final MappingService mappingService;
    private final SessionService sessionService;
    private final StorageService storageService;

    @Override
    public void addTwit(TwitAddRequest request, StreamObserver<TwitItem> responseObserver) {
        var loggedUser = sessionService.getLoggedUser();
        var twit = mappingService.toDoc(request);
        twit.setAuthor(loggedUser);
        twit = storageService.storeTwit(twit);
        var response = mappingService.fromDoc(twit);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTwits(TwitGetRequest request, StreamObserver<TwitGetResponse> responseObserver) {
        var count = request.getCount();
        var items = storageService.getLastTwits(count).stream().map(mappingService::fromDoc);
        responseObserver.onNext(TwitGetResponse.newBuilder().addAllTwit(items::iterator).build());
        responseObserver.onCompleted();
    }
}
