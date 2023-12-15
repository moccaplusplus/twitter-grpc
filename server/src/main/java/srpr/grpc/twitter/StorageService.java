package srpr.grpc.twitter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpr.grpc.twitter.TwitterServiceOuterClass.Twit;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final MongoTemplate mongoTemplate;

    @Transactional
    public TwitItem storeTwit(Twit twit) {
        var doc = toDoc(twit);
        doc.setTimestamp(Instant.now().toEpochMilli());
        mongoTemplate.save(doc);
        return fromDoc(doc);
    }

    @Transactional(readOnly = true)
    public List<TwitItem> getLastTwits(int count) {
        var query = new Query();
        query.with(Sort.sort(TwitDoc.class).by(TwitDoc::getTimestamp).descending());
        query.limit(count);
        var found = mongoTemplate.find(query, TwitDoc.class);
        return found.stream().map(StorageService::fromDoc).toList();
    }

    private static TwitDoc toDoc(Twit twit) {
        return TwitDoc.builder().message(twit.getMessage()).build();
    }

    private static TwitItem fromDoc(TwitDoc doc) {
        return TwitItem.newBuilder()
                .setMessage(doc.getMessage())
                .setTimestamp(doc.getTimestamp())
                .build();
    }
}
