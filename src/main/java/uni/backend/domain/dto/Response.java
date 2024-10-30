package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Response {

    private String status;
    private String message;

    public static Response successMessage(String message) {
        return new Response("success", message);
    }

    public static Response failMessage(String message) {
        return new Response("fail", message);
    }

}