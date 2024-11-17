package uni.backend.domain.dto;

import lombok.Data;
import uni.backend.enums.LanguageAbbrev;

@Data
public class CreateGlossaryRequest {

    String name;
    String source_lang;
    String target_lang;
    String entries;
    String entries_format;
}
