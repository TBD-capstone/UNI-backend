package uni.backend.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uni.backend.enums.LanguageAbbrev;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranslationRequest {

    private List<String> text;
    private String source_lang;
    private String target_lang;
    private String context;
    private String split_sentences;
    private Boolean preserve_formatting;
    private String formality;
    private String glossary_id;
    private String tag_handling;
}
