package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import lombok.ToString;
import uni.backend.enums.LanguageAbbrev;

@Getter
@Setter
@ToString
public class IndividualTranslationResponse {

    private String detected_source_language;
    private String text;
}