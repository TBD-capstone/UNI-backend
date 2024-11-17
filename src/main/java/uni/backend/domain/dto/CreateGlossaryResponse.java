package uni.backend.domain.dto;

import java.time.LocalDateTime;
import lombok.Data;
import uni.backend.enums.LanguageAbbrev;

@Data
public class CreateGlossaryResponse {

    String glossary_id;
    String name;
    Boolean ready;
    String source_lang;
    String target_lang;
    LocalDateTime creation_time;
    Integer entry_count;
}
