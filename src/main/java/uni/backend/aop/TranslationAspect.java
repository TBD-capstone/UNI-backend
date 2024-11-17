package uni.backend.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.domain.dto.IndividualTranslationResponse;
import uni.backend.domain.dto.TranslationRequest;
import uni.backend.domain.dto.TranslationResponse;
import uni.backend.service.TranslationService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
@Slf4j
public class TranslationAspect {

    @Autowired
    private TranslationService translationService;

    @Pointcut("execution(* uni.backend.controller.HomeController.*(..))")
    public void homeControllerMethods() {
    }

    @Pointcut("execution(* uni.backend.controller.ProfileController.*(..))")
    public void profileControllerMethods() {
    }

    @Pointcut("homeControllerMethods() || profileControllerMethods()")
    public void toTranslateControllerMethods() {
    }

    // 메서드 실행 후 응답 값을 번역
    @AfterReturning(pointcut = "toTranslateControllerMethods()", returning = "response")
    public void translateResponse(JoinPoint joinPoint, Object response) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(
            RequestContextHolder.getRequestAttributes())).getRequest();
        String acceptLanguage = translationService.determineTargetLanguage(
            Optional.ofNullable(request.getHeader("Accept-Language"))
                .orElse(TranslationService.DEFAULT_LANGUAGE));
        if (acceptLanguage.equals(TranslationService.DEFAULT_LANGUAGE)) {
            return;
        }

        if (response instanceof ResponseEntity) {
            Object body = ((ResponseEntity<?>) response).getBody();
            if (body instanceof HomeDataResponse) {
                translateHomeDataResponse((HomeDataResponse) body, acceptLanguage);
            } else if (body instanceof IndividualProfileResponse) {
                translateProfileResponse((IndividualProfileResponse) body, acceptLanguage);
            }
        }
    }

    private List<String> extractUnivHashtag(String univName, List<String> hashtags,
        String acceptLanguage, String content) {
        TranslationRequest translationRequest = new TranslationRequest();
        List<String> newList = new ArrayList<>(hashtags);
        if (content != null) {
            newList.addFirst(content);
        }
        newList.addFirst(univName);
        translationRequest.setText(newList);
        translationRequest.setSource_lang("KO");
        translationRequest.setTarget_lang(acceptLanguage);
        TranslationResponse translationResponse = translationService.translate(
            translationRequest);
        return translationResponse.getTranslations()
            .stream()
            .map(IndividualTranslationResponse::getText)
            .collect(Collectors.toList());
    }

    private void translateHomeDataResponse(HomeDataResponse homeDataResponse,
        String acceptLanguage) {
        if (homeDataResponse == null || homeDataResponse.getData() == null) {
            return;
        }

        for (HomeProfileResponse profile : homeDataResponse.getData()) {
            List<String> data = extractUnivHashtag(profile.getUnivName(), profile.getHashtags(),
                acceptLanguage, null);
            profile.setUnivName(data.getFirst());
            if (data.size() > 1) {
                profile.setHashtags(data.subList(1, data.size()));
            }
        }
    }

    private void translateProfileResponse(IndividualProfileResponse individualProfileResponse,
        String acceptLanguage) {
        if (individualProfileResponse == null) {
            return;
        }

        List<String> data = extractUnivHashtag(individualProfileResponse.getUniv(),
            individualProfileResponse.getHashtags(), acceptLanguage,
            individualProfileResponse.getDescription());
        individualProfileResponse.setUniv(data.getFirst());
        individualProfileResponse.setDescription(data.get(1));
        if (data.size() > 2) {
            individualProfileResponse.setHashtags(data.subList(2, data.size()));
        }
    }
}
