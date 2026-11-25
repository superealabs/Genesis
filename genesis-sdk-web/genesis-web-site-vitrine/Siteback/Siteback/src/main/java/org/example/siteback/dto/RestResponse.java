package org.example.siteback.dto;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse<T> {
    private int status;
    private String message;
    private int returnCode;
    private T data;
    private T errors;
    private Instant timestamp;

    private RestResponse(int status, String message, int returnCode, T data) {
        this.status = status;
        this.message = message;
        this.returnCode = returnCode;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> RestResponse<T> buildSuccessResponse(HttpStatus status, String message, T data) {
        int OK = 1;
        return new RestResponse<>(status.value(), message, OK, data);
    }

    public static <T> RestResponse<T> buildErrorResponse(HttpStatus status, String message, T errors) {
        int KO = 0;
        RestResponse ans= new RestResponse<>(status.value(), message, KO, null);
        ans.setErrors(errors);
        return ans;
    }
}
