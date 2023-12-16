package srpr.grpc.twitter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import srpr.grpc.twitter.TwitterServiceOuterClass.Author;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitAddRequest;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;
import srpr.grpc.twitter.document.TwitDoc;
import srpr.grpc.twitter.document.UserDoc;

@Service
@RequiredArgsConstructor
public class MappingService {
    public TwitDoc toDoc(TwitAddRequest twit) {
        return TwitDoc.builder().message(twit.getMessage()).build();
    }

    public TwitItem fromDoc(TwitDoc doc) {
        return TwitItem.newBuilder()
                .setMessage(doc.getMessage())
                .setTimestamp(doc.getTimestamp())
                .setAuthor(fromDoc(doc.getAuthor()))
                .build();
    }

    public Author fromDoc(UserDoc doc) {
        return Author.newBuilder()
                .setName(doc.getUserName())
                .setEmail(doc.getEmail())
                .build();
    }
}
