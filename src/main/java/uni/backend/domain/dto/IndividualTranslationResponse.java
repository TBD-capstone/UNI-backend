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
public class IndividualTranslationResponse {

    private String detected_source_language;
    private String text;
}