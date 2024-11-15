package uni.backend.domain;

import lombok.EqualsAndHashCode;
import java.io.Serializable;

@EqualsAndHashCode
public class MatchingJoinId implements Serializable {
    private Integer user;
    private Integer matching;
}
