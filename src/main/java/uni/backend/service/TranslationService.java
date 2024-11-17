package uni.backend.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.usertype.BaseUserTypeSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;
import uni.backend.domain.dto.CreateGlossaryRequest;
import uni.backend.domain.dto.CreateGlossaryResponse;
import uni.backend.domain.dto.GlossariesListResponse;
import uni.backend.domain.dto.SingleGlossaryResponse;
import uni.backend.domain.dto.TranslationRequest;
import uni.backend.domain.dto.TranslationResponse;
import uni.backend.enums.LanguageAbbrev;
import uni.backend.exception.DeeplWrongFormatException;

@Slf4j
@Service
public class TranslationService {

    public static final String DEFAULT_LANGUAGE = "ko";
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("en", "ko", "zh");
    private static final String DEEPL_TRANSLATE_URL = "https://api-free.deepl.com/v2/translate";
    private static final String DEEPL_TRANSLATE_DOCUMENT_URL = "https://api-free.deepl.com/v2/document";
    private static final String DEEPL_GLOSSARY_URL = "https://api-free.deepl.com/v2/glossaries";
    private static final String DEEPL_GLOSSARY_ENTRY_URL = "https://api-free.deepl.com/v2/glossaries";

    @Autowired
    private final RestClient restClient;

    @Value("${DeepL.key}")
    private String authKey;

    @Value("${DeepL.glossary.en}")
    private String glossaryEn;

    @Value("${DeepL.glossary.zh}")
    private String glossaryZh;

    public TranslationService(RestClient restClient) {
        this.restClient = restClient;
    }

//    public String determineTargetLanguage(String acceptLanguage) {
//        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
//            return DEFAULT_LANGUAGE;
//        }
//
//        return Arrays.stream(acceptLanguage.split(",")).map(lang -> lang.split(";")[0].trim())
//            .filter(SUPPORTED_LANGUAGES::contains).findFirst().orElse(DEFAULT_LANGUAGE);
//    }

    public String determineTargetLanguage(String acceptLanguageHeader) {
        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            return DEFAULT_LANGUAGE; // Default if header is null or empty
        }

        return Arrays.stream(acceptLanguageHeader.split(","))
            .map(String::trim) // Remove whitespace
            .map(this::parseLanguageEntry) // Parse each language entry
            .filter(Objects::nonNull) // Remove invalid entries
            .sorted(Comparator.comparingDouble((LanguageEntry e) -> e.qValue)
                .reversed()) // Sort by q-value (descending)
            .map(entry -> getBaseLanguage(
                entry.language)) // Extract base language (e.g., "en-US" -> "en")
            .filter(SUPPORTED_LANGUAGES::contains) // Keep only supported languages
            .findFirst() // Get the highest-priority language
            .orElse(DEFAULT_LANGUAGE); // Default if none match
    }

    private LanguageEntry parseLanguageEntry(String entry) {
        String[] parts = entry.split(";"); // Split language and q-value
        String language = parts[0].trim(); // First part is the language code
        double qValue = 1.0; // Default q-value

        // If q-value is provided, parse it
        if (parts.length > 1 && parts[1].trim().startsWith("q=")) {
            try {
                qValue = Double.parseDouble(parts[1].trim().substring(2));
            } catch (NumberFormatException e) {
                return null; // Invalid q-value; ignore this entry
            }
        }

        return new LanguageEntry(language, qValue);
    }

    private String getBaseLanguage(String language) {
        // Extract base language (e.g., "en-US" -> "en")
        if (language.contains("-")) {
            return language.split("-")[0];
        }
        return language.toLowerCase();
    }

    // Helper class to hold parsed language and q-value
    private class LanguageEntry {

        String language;
        double qValue;

        LanguageEntry(String language, double qValue) {
            this.language = language;
            this.qValue = qValue;
        }
    }

    public TranslationResponse translate(TranslationRequest request) {

        final String sourceLang = request.getSource_lang();
        final String targetLang = request.getTarget_lang();

        if (targetLang == null) {
            throw new DeeplWrongFormatException("Needs source_lang");
        }
        normalizeLanguages(request);
        applyGlossaryIfNeeded(request, sourceLang, targetLang);

        return sendTranslationRequest(request);
    }

    private void normalizeLanguages(TranslationRequest request) {
        request.setTarget_lang(request.getTarget_lang().toUpperCase());
        if (request.getSource_lang() != null) {
            request.setSource_lang(request.getSource_lang().toUpperCase());
        }
    }

    private void applyGlossaryIfNeeded(TranslationRequest request, String sourceLang,
        String targetLang) {
        if (sourceLang != null && sourceLang.equalsIgnoreCase("KO")) {
            if (targetLang.equalsIgnoreCase("EN")) {
                request.setGlossary_id(glossaryEn);
            } else if (targetLang.equalsIgnoreCase("ZH")) {
                request.setGlossary_id(glossaryZh);
            }
        }
    }

//    public translateHtml() {
//
//    }

    private TranslationResponse sendTranslationRequest(TranslationRequest request) {
        return restClient.post().uri(DEEPL_TRANSLATE_URL)
            .header("Authorization", "DeepL-Auth-Key " + authKey).body(request).retrieve()
            .body(TranslationResponse.class);
    }


    public CreateGlossaryResponse createGlossary(CreateGlossaryRequest request) {
        CreateGlossaryResponse response = restClient.post().uri(DEEPL_GLOSSARY_URL)
            .header("Authorization", "DeepL-Auth-Key " + authKey).body(request).retrieve()
            .body(CreateGlossaryResponse.class);

        return response;
    }

    public GlossariesListResponse getGlossariesList() {

        return restClient.get().uri(DEEPL_GLOSSARY_URL)
            .header("Authorization", "DeepL-Auth-Key " + authKey).retrieve()
            .body(GlossariesListResponse.class);
    }

    public SingleGlossaryResponse retrieveGlossaryEntry(String glossaryId) {
        return restClient.get().uri(DEEPL_GLOSSARY_ENTRY_URL + "/" + glossaryId + "/entries")
            .header("Authorization", "DeepL-Auth-Key " + authKey).retrieve()
            .body(SingleGlossaryResponse.class);
    }
}
