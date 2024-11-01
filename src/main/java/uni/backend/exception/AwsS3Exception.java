package uni.backend.exception;


import lombok.Getter;
import uni.backend.enums.AwsS3ErrorCode;

@Getter
public class AwsS3Exception extends RuntimeException {

    private String message;
    private AwsS3ErrorCode errorCode;

    public AwsS3Exception(AwsS3ErrorCode errorCode) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }
}
