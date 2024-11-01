package uni.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uni.backend.exception.AwsS3Exception;
import uni.backend.service.AwsS3Service;

@RestController
@RequestMapping("/api/s3")
public class AwsS3Controller {

    private static final Logger log = LoggerFactory.getLogger(AwsS3Controller.class);

    @Autowired
    private AwsS3Service awsS3Service;

    @PostMapping("/upload")
    public ResponseEntity<?> s3Upload(
        @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            String profileImage = awsS3Service.upload(image);
            return ResponseEntity.ok(profileImage);
        } catch (AwsS3Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }

    }

    // 삭제할 일이 없어보입니다..
//    @DeleteMapping("/delete")
//    public ResponseEntity<?> s3Delete(@RequestParam String addr) {
//        awsS3Service.deleteImageFromS3(addr);
//        return ResponseEntity.ok(null);
//    }
}
