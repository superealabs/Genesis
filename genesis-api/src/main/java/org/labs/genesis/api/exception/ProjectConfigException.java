package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProjectConfigException extends RuntimeException {

    public ProjectConfigException(String message) {
        super(message);
    }
}