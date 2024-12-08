package uni.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import uni.backend.config.TestSecurityConfig;
import uni.backend.security.JwtUtils;
import uni.backend.service.HomeService;
import uni.backend.service.PageTranslationService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HomeController.class, excludeAutoConfiguration = {
    ThymeleafAutoConfiguration.class})
@Import(TestSecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeService homeService;

    @MockBean
    private PageTranslationService pageTranslationService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("/home 엔드포인트 호출 테스트 - univName과 hashtags 처리 확인")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void testHomeEndpointWithParams() throws Exception {
        // Case 1: univName is null, hashtags is null
        mockMvc.perform(get("/api/home")
                .param("page", "0")
                .param("sort", "newest"))
            .andExpect(status().isOk())
            .andDo(print());

        // Case 2: univName is not null, with spaces
        mockMvc.perform(get("/api/home")
                .param("univName", "   Test University   ")
                .param("page", "0")
                .param("sort", "newest"))
            .andExpect(status().isOk())
            .andDo(print());

        // Case 3: hashtags is not null
        mockMvc.perform(get("/api/home")
                .param("hashtags", "tag1,tag2,tag3")
                .param("page", "0")
                .param("sort", "newest"))
            .andExpect(status().isOk())
            .andDo(print());

        // Case 4: Both univName and hashtags are present
        mockMvc.perform(get("/api/home")
                .param("univName", "Test University")
                .param("hashtags", "tag1,tag2,tag3")
                .param("page", "0")
                .param("sort", "newest"))
            .andExpect(status().isOk())
            .andDo(print());
    }
}