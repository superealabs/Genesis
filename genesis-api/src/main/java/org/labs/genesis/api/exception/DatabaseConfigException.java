package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DatabaseConfigException extends RuntimeException {
    private final String code;
    public DatabaseConfigException(String message) {
        super(message);
        this.code = "INVALID_GENERATION_CONFIGURATION";
    }

    public DatabaseConfigException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}