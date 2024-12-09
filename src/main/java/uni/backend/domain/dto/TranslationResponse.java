package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import lombok.ToString;
import uni.backend.enums.LanguageAbbrev;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class TranslationResponse {

    private List<IndividualTranslationResponse> translations;
}