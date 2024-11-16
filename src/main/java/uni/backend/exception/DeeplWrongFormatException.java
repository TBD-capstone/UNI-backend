package uni.backend.exception;

import lombok.Getter;
import uni.backend.enums.AwsS3ErrorCode;

@Getter
public class DeeplWrongFormatException extends RuntimeException {

    private String message;

    public DeeplWrongFormatException(String message) {
        super(message);
        this.message = message;
    }
}