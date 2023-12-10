package srpr.grpc.twitter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import srpr.grpc.twitter.TwitterServiceGrpc.TwitterServiceBlockingStub;
import srpr.grpc.twitter.TwitterServiceOuterClass.Twit;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitGetRequest;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

import java.util.Iterator;
import java.util.stream.Stream;

public class GrpcClient implements AutoCloseable {

    private final ManagedChannel channel;
    private final TwitterServiceBlockingStub stub;

    public GrpcClient(String url) {
        channel = ManagedChannelBuilder.forTarget(url)
                .usePlaintext()
                .build();
        stub = TwitterServiceGrpc.newBlockingStub(channel);
    }

    public TwitItem send(String message) {
        var request = Twit.newBuilder().setMessage(message).build();
        return stub.addTwit(request);
    }

    public Stream<TwitItem> get(int count) {
        var request = TwitGetRequest.newBuilder().setCount(count).build();
        var response = stub.getTwits(request);
        return Stream.generate(() -> response).takeWhile(Iterator::hasNext).map(Iterator::next);
    }

    @Override
    public void close() {
        channel.shutdown();
    }
}
