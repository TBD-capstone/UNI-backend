package uni.backend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AwsS3ErrorCode {
    EMPTY_FILE_EXCEPTION("The file is empty."),
    IO_EXCEPTION_ON_IMAGE_UPLOAD("An I/O error occurred during image upload."),
    NO_FILE_EXTENTION("The file has no extension."),
    INVALID_FILE_EXTENTION("The file extension is invalid."),
    PUT_OBJECT_EXCEPTION("Failed to put the object in S3."),
    IO_EXCEPTION_ON_IMAGE_DELETE("An I/O error occurred during image delete.");

    private final String message;
}
