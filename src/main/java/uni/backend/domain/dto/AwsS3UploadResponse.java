package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AwsS3UploadResponse {

    private String imageUrl;
}
