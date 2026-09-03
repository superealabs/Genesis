package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DatabaseConfigException extends RuntimeException {

    public DatabaseConfigException(String message) {
        super(message);
    }
}