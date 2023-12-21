package srpr.grpc.twitter;

import io.grpc.CallCredentials;
import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import srpr.grpc.twitter.TwitterServiceGrpc.TwitterServiceBlockingStub;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitAddRequest;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitGetRequest;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

import java.util.List;

public class GrpcClient implements AutoCloseable {
    private final ManagedChannel channel;
    private final TwitterServiceBlockingStub stub;

    public GrpcClient(String host, int port, ChannelCredentials channelCredentials, CallCredentials callCredentials) {
        channel = Grpc.newChannelBuilderForAddress(host, port, channelCredentials)
                .build();
        stub = TwitterServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(callCredentials);
    }

    public TwitItem send(String message) {
        var request = TwitAddRequest.newBuilder().setMessage(message).build();
        return stub.addTwit(request);
    }

    public List<TwitItem> get(int count) {
        var request = TwitGetRequest.newBuilder().setCount(count).build();
        var response = stub.getTwits(request);
        return response.getTwitList();
    }

    @Override
    public void close() {
        channel.shutdown();
    }
}
