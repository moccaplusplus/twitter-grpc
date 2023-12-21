package srpr.grpc.twitter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import srpr.grpc.twitter.document.TwitDoc;
import srpr.grpc.twitter.document.UserDoc;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final MongoOperations mongoOperations;

    @Transactional
    public UserDoc saveUser(UserDoc user) {
        var doc = findUser(user.getName());
        if (doc != null) user.setId(doc.getId());
        return mongoOperations.save(user);
    }

    @Transactional(readOnly = true)
    public UserDoc findUser(String name) {
        var query = new Query();
        query.addCriteria(Criteria.where("name").is(name));
        return mongoOperations.findOne(query, UserDoc.class);
    }

    @Transactional
    public TwitDoc storeTwit(TwitDoc twit) {
        twit.setTimestamp(Instant.now().toEpochMilli());
        return mongoOperations.save(twit);
    }

    @Transactional(readOnly = true)
    public List<TwitDoc> getLastTwits(int count) {
        var query = new Query();
        query.with(Sort.sort(TwitDoc.class).by(TwitDoc::getTimestamp).descending());
        query.limit(count);
        return mongoOperations.find(query, TwitDoc.class);
    }
}
