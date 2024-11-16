package uni.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uni.backend.domain.dto.CreateGlossaryRequest;
import uni.backend.domain.dto.CreateGlossaryResponse;
import uni.backend.domain.dto.GlossariesListResponse;
import uni.backend.domain.dto.SingleGlossaryResponse;
import uni.backend.domain.dto.TranslationRequest;
import uni.backend.domain.dto.TranslationResponse;
import uni.backend.service.TranslationService;
// import uni.backend.service.TranslationService;

@RestController
@RequestMapping("/api")
class TranslationController {

    private static final Logger log = LoggerFactory.getLogger(TranslationController.class);

    @Autowired
    private TranslationService translationService;

    // 단순 번역
    @PostMapping("/translate")
    public ResponseEntity<TranslationResponse> translate(@RequestBody TranslationRequest request,
        HttpServletRequest httpRequest) {
        String targetLanguage = translationService.determineTargetLanguage(
            httpRequest.getHeader("Accept-Language"));

        TranslationResponse response = translationService.translate(request, "KO", targetLanguage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/glossary/create")
    public ResponseEntity<CreateGlossaryResponse> createGlossary(
        @RequestBody CreateGlossaryRequest request) {
        CreateGlossaryResponse response = translationService.createGlossary(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/glossary/list")
    public ResponseEntity<GlossariesListResponse> listGlossaries() {
        GlossariesListResponse response = translationService.getGlossariesList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/glossary/retrieve/{glossary_id}")
    public ResponseEntity<SingleGlossaryResponse> retrieveGlossaryEntry(
        @PathVariable String glossary_id) {
        SingleGlossaryResponse response = translationService.retrieveGlossaryEntry(glossary_id);
        return ResponseEntity.ok(response);
    }
}