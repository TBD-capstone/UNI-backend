package uni.backend.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.config.TestSecurityConfig;
import uni.backend.domain.dto.AwsS3UploadResponse;
import uni.backend.domain.dto.Response;
import uni.backend.enums.AwsS3ErrorCode;
import uni.backend.exception.AwsS3Exception;
import uni.backend.security.JwtUtils;
import uni.backend.service.AwsS3Service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AwsS3Controller.class)
@Import(TestSecurityConfig.class)
class AwsS3ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AwsS3Service awsS3Service;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("AWS S3 업로드 성공")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldUploadImageSuccessfully() throws Exception {
        // given
        String imageUrl = "https://s3.amazonaws.com/example-bucket/example-image.jpg";
        Mockito.when(awsS3Service.upload(any(MultipartFile.class), eq("profile"), eq(1)))
            .thenReturn(imageUrl);

        // when & then
        mockMvc.perform(multipart("/api/s3/upload")
                .file("image", "dummy-image-content".getBytes())
                .param("type", "profile")
                .param("userId", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.imageUrl").value(imageUrl));

        Mockito.verify(awsS3Service).upload(any(MultipartFile.class), eq("profile"), eq(1));
    }

    @Test
    @DisplayName("AWS S3 업로드 실패")
    @WithMockUser(username = "korean", roles = {"KOREAN"})
    void shouldHandleUploadFailure() throws Exception {
        // given
        AwsS3ErrorCode errorCode = AwsS3ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD;
        Mockito.when(awsS3Service.upload(any(MultipartFile.class), eq("profile"), eq(1)))
            .thenThrow(new AwsS3Exception(errorCode));

        // when & then
        mockMvc.perform(multipart("/api/s3/upload")
                .file("image", "dummy-image-content".getBytes())
                .param("type", "profile")
                .param("userId", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("fail")) // 수정: JSON의 `status` 필드 확인
            .andExpect(
                jsonPath("$.message").value(errorCode.getMessage())); // 수정: JSON의 `message` 필드 확인

        Mockito.verify(awsS3Service).upload(any(MultipartFile.class), eq("profile"), eq(1));
    }
}