package uni.backend.domain.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class GlossaryResponse {

    String glossary_id;
    Boolean ready;
    String name;
    String source_lang;
    String target_lang;
    LocalDateTime creation_time;
    Integer entry_count;
}
