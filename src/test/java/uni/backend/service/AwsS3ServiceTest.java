package uni.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.enums.AwsS3ErrorCode;
import uni.backend.exception.AwsS3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AwsS3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private AwsS3Service awsS3Service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("빈 파일 업로드 시 예외 발생")
    @Test
    void givenEmptyFile_whenUpload_thenThrowEmptyFileException() {
        // given
        when(image.isEmpty()).thenReturn(true);

        // when & then
        AwsS3Exception exception = assertThrows(AwsS3Exception.class,
            () -> awsS3Service.upload(image, "profile", 1));
        assertEquals(AwsS3ErrorCode.EMPTY_FILE_EXCEPTION, exception.getErrorCode());
    }

    @DisplayName("유효하지 않은 파일 확장자 업로드 시 예외 발생")
    @Test
    void givenInvalidFileExtension_whenUpload_thenThrowInvalidFileExtension() {
        // given
        when(image.getOriginalFilename()).thenReturn("file.txt");
        when(image.isEmpty()).thenReturn(false);

        // when & then
        AwsS3Exception exception = assertThrows(AwsS3Exception.class,
            () -> awsS3Service.upload(image, "profile", 1));
        assertEquals(AwsS3ErrorCode.INVALID_FILE_EXTENTION, exception.getErrorCode());
    }

    @DisplayName("잘못된 타입 업로드 시 예외 발생")
    @Test
    void givenInvalidType_whenUpload_thenThrowIllegalArgumentException() {
        // given
        MultipartFile image = mock(MultipartFile.class);
        when(image.getOriginalFilename()).thenReturn("image.png");
        when(image.isEmpty()).thenReturn(false);

        String invalidType = "invalidType";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            awsS3Service.upload(image, invalidType, 1);  // Testing the public method
        });
        assertEquals("Invalid type: " + invalidType, exception.getMessage());
    }

    @DisplayName("이미지 삭제 성공")
    @Test
    void givenValidImage_whenDelete_thenDeleteImageFromS3() {
        // given
        String imageAddress = "https://example.com/s3-bucket/file.png";
        doNothing().when(amazonS3).deleteObject(any());

        // when
        awsS3Service.deleteImageFromS3(imageAddress);

        // then
        verify(amazonS3).deleteObject(any());
    }

    @DisplayName("잘못된 URL로 이미지 삭제 시 예외 발생")
    @Test
    void givenInvalidUrl_whenDelete_thenThrowAwsS3Exception() {
        // given
        String invalidUrl = "invalid-url";

        // when & then
        AwsS3Exception exception = assertThrows(AwsS3Exception.class,
            () -> awsS3Service.deleteImageFromS3(invalidUrl));
        assertEquals(AwsS3ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE, exception.getErrorCode());
    }

    @DisplayName("빈 광고 이미지 업로드 시 예외 발생")
    @Test
    void givenEmptyAdImage_whenUploadAdImage_thenThrowEmptyFileException() {
        // given
        when(image.isEmpty()).thenReturn(true);

        // when & then
        AwsS3Exception exception = assertThrows(AwsS3Exception.class,
            () -> awsS3Service.uploadAdImage(image));
        assertEquals(AwsS3ErrorCode.EMPTY_FILE_EXCEPTION, exception.getErrorCode());
    }
}