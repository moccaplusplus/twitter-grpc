package srpr.grpc.twitter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("twit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwitDoc {
    @Id
    private String id;

    private String message;

    private long timestamp;
}
