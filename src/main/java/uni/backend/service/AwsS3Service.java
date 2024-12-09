package uni.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.enums.AwsS3ErrorCode;
import uni.backend.exception.AwsS3Exception;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    /**
     * S3 버킷에 저장된 이미지의 URL을 반환하는 메서드
     *
     * @param filePath S3에 저장된 이미지의 경로
     * @return S3 이미지 URL
     */
    public String getImageUrl(String filePath) {
        return amazonS3.getUrl(bucketName, filePath).toString();
    }


    public String upload(MultipartFile image, String type, Integer userId) {
        log.info(bucketName);
        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new AwsS3Exception(AwsS3ErrorCode.EMPTY_FILE_EXCEPTION);
        }
        return this.uploadImage(image, type, userId);
    }

    private String uploadImage(MultipartFile image, String type, Integer userId) {
        this.validateImageFileExtention(image.getOriginalFilename());
        try {
            return this.uploadImageToS3(image, type, userId);
        } catch (IOException e) {
            throw new AwsS3Exception(AwsS3ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }

    private void validateImageFileExtention(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new AwsS3Exception(AwsS3ErrorCode.NO_FILE_EXTENTION);
        }

        String extention = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtentionList = Arrays.asList("jpg", "jpeg", "png");

        if (!allowedExtentionList.contains(extention)) {
            throw new AwsS3Exception(AwsS3ErrorCode.INVALID_FILE_EXTENTION);
        }
    }

    private String uploadImageToS3(MultipartFile image, String type, Integer userId)
        throws IOException {
        // 타입 값이 들어왔을 때, 값이 정확한지 체크
        log.info("Received type: '{}'", type);

        // null 또는 빈 문자열 검사
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("type cannot be null or empty");
        }

        // `type`에 맞는 폴더 구분 (profile / background)
        String s3Folder;

        // 정확하게 "profile"과 "background"를 처리
        if ("profile".equalsIgnoreCase(type)) {
            s3Folder = "profiles/";
        } else if ("background".equalsIgnoreCase(type)) {
            s3Folder = "backgrounds/";
        } else if ("ads".equalsIgnoreCase(type)) { // 광고 타입 추가
            s3Folder = "ads/";
        } else {
            // type 값이 "profile"이나 "background"가 아닐 경우 예외 처리
            throw new IllegalArgumentException("Invalid type: " + type);
        }

        // 폴더 경로가 정확히 설정되었는지 로그로 확인
        log.info("S3 folder selected: {}", s3Folder);

        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 고유한 파일 이름 생성 (UUID + 유저 ID + 확장자 포함)
        String s3FileName =
            s3Folder + "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 10)
                + extension;

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);  // MIME 타입 설정
        metadata.setContentLength(bytes.length);  // 파일 크기 설정
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            // S3에 이미지 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName,
                byteArrayInputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);  // 공개 읽기 권한 설정
            amazonS3.putObject(putObjectRequest);  // 이미지 S3에 업로드
        } catch (Exception e) {
            throw new AwsS3Exception(AwsS3ErrorCode.PUT_OBJECT_EXCEPTION);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        // 업로드된 이미지의 URL 반환
        return getImageUrl(s3FileName);  // getImageUrl 메서드가 경로에 맞는 URL 반환
    }

    public void deleteImageFromS3(String imageAddress) {
        String key = getKeyFromImageAddress(imageAddress);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e) {
            throw new AwsS3Exception(AwsS3ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    private String getKeyFromImageAddress(String imageAddress) {
        try {
            URL url = new URL(imageAddress);
            String decodingKey = URLDecoder.decode(url.getPath(), "UTF-8");
            return decodingKey.substring(1); // 맨 앞의 '/' 제거
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new AwsS3Exception(AwsS3ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    public String uploadAdImage(MultipartFile image) {
        log.info("Uploading ad image to bucket: {}", bucketName);

        if (image.isEmpty() || Objects.isNull(image.getOriginalFilename())) {
            throw new AwsS3Exception(AwsS3ErrorCode.EMPTY_FILE_EXCEPTION);
        }

        this.validateImageFileExtention(image.getOriginalFilename());

        try {
            return this.uploadImageToS3ForAds(image);
        } catch (IOException e) {
            throw new AwsS3Exception(AwsS3ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }

    private String uploadImageToS3ForAds(MultipartFile image) throws IOException {
        String s3Folder = "ads/";

        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        String s3FileName = s3Folder + UUID.randomUUID().toString().substring(0, 10) + extension;

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + extension);  // MIME 타입 설정
        metadata.setContentLength(bytes.length);  // 파일 크기 설정
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            // S3에 이미지 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3FileName,
                byteArrayInputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);  // 공개 읽기 권한 설정
            amazonS3.putObject(putObjectRequest);  // 이미지 S3에 업로드
        } catch (Exception e) {
            throw new AwsS3Exception(AwsS3ErrorCode.PUT_OBJECT_EXCEPTION);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        // 업로드된 이미지의 URL 반환
        return getImageUrl(s3FileName);  // getImageUrl 메서드가 경로에 맞는 URL 반환
    }


}

/*
https://innovation123.tistory.com/197#S3%20ImageService-1
위 링크를 참조했습니다.

 */