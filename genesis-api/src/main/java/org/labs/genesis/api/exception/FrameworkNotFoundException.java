package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FrameworkNotFoundException extends RuntimeException {

    public FrameworkNotFoundException(int id) {
        super("Framework introuvable : " + id);
    }
}