package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RelationException extends RuntimeException {

    public RelationException(String message) {
        super(message);
    }
}
