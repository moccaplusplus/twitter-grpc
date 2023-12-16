package srpr.grpc.twitter.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.security.Principal;

@Document("user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDoc implements Principal {
    @Id
    private String id;

    private String name;

    private String givenName;

    private String familyName;

    private String email;

    public String getUserName() {
        return givenName + " " + familyName;
    }
}
