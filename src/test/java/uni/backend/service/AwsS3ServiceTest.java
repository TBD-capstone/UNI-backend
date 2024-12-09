package uni.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.net.MalformedURLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.net.URL;
import uni.backend.exception.AwsS3Exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AwsS3ServiceTest {

    @InjectMocks
    private AwsS3Service awsS3Service;

    @Mock
    private AmazonS3 amazonS3;

    private final String bucketName = "test-bucket"; // Mock용 S3 버킷 이름
    private MockMultipartFile mockMultipartFile;

    @BeforeEach
    void setUp() throws MalformedURLException {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(awsS3Service, "bucketName", "test-bucket");

        // getUrl Mock 설정
        when(amazonS3.getUrl(eq("test-bucket"), anyString()))
            .thenAnswer(invocation -> {
                String filePath = invocation.getArgument(1);
                return new URL("https://mock-s3-url.com/" + filePath);
            });
        mockMultipartFile = new MockMultipartFile(
            "file",                        // 필드 이름
            "test-image.jpg",              // 파일 이름
            "image/jpeg",                  // 파일 타입
            "test image content".getBytes() // 파일 내용
        );
    }


    @Test
    @DisplayName("광고 이미지 업로드 - 성공")
    void uploadAdImage_Success() throws Exception {
        // given
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("ad-image.png");
        when(mockMultipartFile.getInputStream()).thenReturn(
            new ByteArrayInputStream("test data".getBytes()));

        String filePath = "ads/c20d92e7-f.png";
        when(amazonS3.getUrl(bucketName, filePath)).thenReturn(
            new URL("https://mock-s3-url.com/" + filePath));

        // when
        String result = awsS3Service.uploadAdImage(mockMultipartFile);

        // then
        assertNotNull(result);
        assertTrue(result.contains("https://mock-s3-url.com/ads/"));
        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("이미지 업로드 - 성공")
    void uploadImage_Success() throws Exception {
        // given
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test-image.png");
        when(mockMultipartFile.getInputStream()).thenReturn(
            new ByteArrayInputStream("test data".getBytes()));

        String filePath = "profiles/user_1_a2d9e167-f.png";
        when(amazonS3.getUrl(bucketName, filePath)).thenReturn(
            new URL("https://mock-s3-url.com/" + filePath));

        // when
        String result = awsS3Service.upload(mockMultipartFile, "profile", 1);

        // then
        assertNotNull(result);
        assertTrue(result.contains("https://mock-s3-url.com/profiles/"));
        verify(amazonS3).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("이미지 삭제 - 성공")
    void deleteImageFromS3_Success() {
        // given
        String imageAddress = "https://mock-s3-url.com/profiles/user_1_a2d9e167-f.png";

        // when
        awsS3Service.deleteImageFromS3(imageAddress);

        // then
        verify(amazonS3).deleteObject(argThat(request ->
            request instanceof DeleteObjectRequest &&
                ((DeleteObjectRequest) request).getKey().equals("profiles/user_1_a2d9e167-f.png")
        ));
    }

    @Test
    @DisplayName("파일 확장자 검증 - 실패")
    void validateImageFileExtension_Fail() {
        // given
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test-image.invalid");

        // then
        assertThrows(AwsS3Exception.class, () -> {
            awsS3Service.upload(mockMultipartFile, "profile", 1);
        });
    }

    @Test
    @DisplayName("이미지 URL 추출 실패 - 잘못된 URL")
    void getKeyFromImageAddress_Fail() {
        // given
        String invalidImageAddress = "invalid-url";

        // then
        assertThrows(AwsS3Exception.class, () -> {
            awsS3Service.deleteImageFromS3(invalidImageAddress);
        });
    }

    @Test
    @DisplayName("유효한 type 값 - profile")
    void uploadImageToS3_ValidType_Profile() throws Exception {
        String type = "profile";
        String result = ReflectionTestUtils.invokeMethod(
            awsS3Service, "uploadImageToS3", mockMultipartFile, type, 1);

        assertNotNull(result);
        assertTrue(result.contains("profiles/"));
    }

    @Test
    @DisplayName("유효한 type 값 - background")
    void uploadImageToS3_ValidType_Background() throws Exception {
        String type = "background";
        String result = ReflectionTestUtils.invokeMethod(
            awsS3Service, "uploadImageToS3", mockMultipartFile, type, 1);

        assertNotNull(result);
        assertTrue(result.contains("backgrounds/"));
    }

    @Test
    @DisplayName("유효한 type 값 - ads")
    void uploadImageToS3_ValidType_Ads() throws Exception {
        String type = "ads";
        String result = ReflectionTestUtils.invokeMethod(
            awsS3Service, "uploadImageToS3", mockMultipartFile, type, 1);

        assertNotNull(result);
        assertTrue(result.contains("ads/"));
    }

    @Test
    @DisplayName("유효하지 않은 type 값")
    void uploadImageToS3_InvalidType() {
        String invalidType = "invalidType";
        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionTestUtils.invokeMethod(
                awsS3Service, "uploadImageToS3", mockMultipartFile, invalidType, 1)
        );

        assertEquals("Invalid type: " + invalidType, exception.getMessage());
    }

    @Test
    @DisplayName("type이 null일 경우")
    void uploadImageToS3_NullType() {
        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionTestUtils.invokeMethod(
                awsS3Service, "uploadImageToS3", mockMultipartFile, null, 1)
        );

        assertEquals("type cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("type이 빈 문자열일 경우")
    void uploadImageToS3_EmptyType() {
        Exception exception = assertThrows(
            IllegalArgumentException.class,
            () -> ReflectionTestUtils.invokeMethod(
                awsS3Service, "uploadImageToS3", mockMultipartFile, "", 1)
        );

        assertEquals("type cannot be null or empty", exception.getMessage());
    }

}
