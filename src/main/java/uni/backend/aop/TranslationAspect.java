package uni.backend.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.aspectj.lang.JoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import uni.backend.domain.dto.HomeDataResponse;
import uni.backend.domain.dto.HomeProfileResponse;
import uni.backend.domain.dto.IndividualProfileResponse;
import uni.backend.domain.dto.TranslationRequest;
import uni.backend.domain.dto.TranslationResponse;
import uni.backend.service.TranslationService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
@Slf4j
public class TranslationAspect {

    private static final Logger log = LoggerFactory.getLogger(TranslationAspect.class);
    @Autowired
    private HttpServletRequest request; // Request Header 확인을 위해 필요
    @Autowired
    private TranslationService translationService;

    // 특정 Controller의 메서드에만 적용 (HomeController)
    @Pointcut("execution(* uni.backend.controller.HomeController.*(..)) && execution(* uni.backend.controller.ProfileController.*(..))")
    public void toTranslateControllerMethods() {
    }

    // 메서드 실행 후 응답 값을 번역
    @AfterReturning(pointcut = "toTranslateControllerMethods()", returning = "response")
    public void translateResponse(JoinPoint joinPoint, Object response) {
        System.out.println("asdf");
        log.info("asdf");
        // Accept-Language 헤더 확인
        String acceptLanguage = request.getHeader("Accept-Language");
        if (translationService.determineTargetLanguage(acceptLanguage)
            .equals(TranslationService.DEFAULT_LANGUAGE)) {
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

    private void translateHomeDataResponse(HomeDataResponse homeDataResponse,
        String acceptLanguage) {
        if (homeDataResponse == null || homeDataResponse.getData() == null) {
            return;
        }

//        String targetLang =
//            TranslationRequest translationRequest = new TranslationRequest();
//        translationRequest.setSource_lang("ko");

        for (HomeProfileResponse profile : homeDataResponse.getData()) {
            System.out.println(profile.getUsername());
            log.info(profile.getUsername());
        }
    }

    private void translateProfileResponse(IndividualProfileResponse individualProfileResponse,
        String acceptLanguage) {

    }
}
