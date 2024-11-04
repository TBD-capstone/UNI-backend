package uni.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.controller.AwsS3Controller;

@SpringBootTest
public class AwsS3ServiceTest {

    @Autowired
    private AwsS3Service awsS3Service;

    @Test
    void S3를_이용한_이미지_업로드() {
//        MultipartFile multipartFile;
//        awsS3Service.upload(multipartFile);
    }
}