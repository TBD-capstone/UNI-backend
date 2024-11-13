package uni.backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import lombok.ToString;
import uni.backend.enums.LanguageAbbrev;

@Getter
@Setter
@ToString
public class TranslationResponse {

    private List<IndividualTranslationResponse> translations;
}