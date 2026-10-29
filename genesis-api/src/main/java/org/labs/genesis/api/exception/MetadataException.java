package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MetadataException extends RuntimeException {

    public MetadataException(String message) {
        super(message);
    }
}
