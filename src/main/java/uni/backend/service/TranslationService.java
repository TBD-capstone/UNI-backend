package uni.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uni.backend.domain.dto.TranslationRequest;
import uni.backend.domain.dto.TranslationResponse;

@Service
public class TranslationService {

    @Autowired
    private final RestClient restClient;

    private static final String DEEPL_TRANSLATE_URL = "https://api-free.deepl.com/v2/translate";

    @Value("${DeepL.key}")
    private String authKey;

    public TranslationService(RestClient restClient) {
        this.restClient = restClient;
    }

    public TranslationResponse translate(TranslationRequest request) {
        String targetLang = String.valueOf(request.getTarget_lang());

        System.out.println(request.toString());

        TranslationResponse response = restClient
            .post()
            .uri(DEEPL_TRANSLATE_URL)
            .header("Authorization", "DeepL-Auth-Key " + authKey)
            .body(request)
            .retrieve()
            .body(TranslationResponse.class);

        System.out.println(response);

        return response;
    }
}
