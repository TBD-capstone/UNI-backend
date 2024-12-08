package uni.backend.controller;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.config.TestSecurityConfig;
import uni.backend.domain.Ad;
import uni.backend.domain.dto.AdListResponse;
import uni.backend.domain.dto.AdRequest;
import uni.backend.domain.dto.UpdateAdStatusRequest;
import uni.backend.enums.AdStatus;
import uni.backend.security.JwtUtils;
import uni.backend.service.AdService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdController.class)
@Import(TestSecurityConfig.class)
class AdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdService adService;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("모든 광고 조회")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnAllAds() throws Exception {
        // given
        AdListResponse adListResponse = AdListResponse.builder()
            .ads(List.of())
            .build();
        Mockito.when(adService.findAll()).thenReturn(adListResponse);

        // when & then
        mockMvc.perform(get("/api/admin/ad"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(adService).findAll();
    }


    @Test
    @DisplayName("새로운 광고 생성")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateNewAd() throws Exception {
        // given
        MockMultipartFile adImg = new MockMultipartFile(
            "adImg", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());
        MockMultipartFile adRequest = new MockMultipartFile(
            "adRequest", "", MediaType.APPLICATION_JSON_VALUE,
            "{\"title\":\"Test Ad\",\"description\":\"Test Description\"}".getBytes());
        Ad createdAd = new Ad();
        Mockito.when(adService.uploadAd(any(MultipartFile.class), any(AdRequest.class)))
            .thenReturn(createdAd);

        // when & then
        mockMvc.perform(multipart("/api/admin/ad/new")
                .file(adImg)
                .file(adRequest)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk());

        Mockito.verify(adService).uploadAd(any(MultipartFile.class), any(AdRequest.class));
    }

    @Test
    @DisplayName("광고 상태 수정")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateAdStatus() throws Exception {
        // given
        UpdateAdStatusRequest request = UpdateAdStatusRequest.builder()
            .adId(1)
            .newStatus(AdStatus.ACTIVE)
            .build();
        Mockito.doNothing().when(adService).updateAdStatus(eq(1), eq(AdStatus.ACTIVE));

        // when & then
        mockMvc.perform(post("/api/admin/ad/update-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"adId\":1,\"newStatus\":\"ACTIVE\"}"))
            .andExpect(status().isOk());

        Mockito.verify(adService).updateAdStatus(eq(1), eq(AdStatus.ACTIVE));
    }

    @Test
    @DisplayName("랜덤 ACTIVE 광고 반환")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldReturnRandomActiveAd() throws Exception {
        // given
        Ad activeAd = new Ad();
        Mockito.when(adService.getRandomActiveAd()).thenReturn(activeAd);

        // when & then
        mockMvc.perform(get("/api/ad"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        Mockito.verify(adService).getRandomActiveAd();
    }
}