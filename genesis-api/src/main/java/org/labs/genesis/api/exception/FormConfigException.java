package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Configuration de formulaire invalide (table, colonne, composant ou paramètre inconnu). */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FormConfigException extends RuntimeException {

    public FormConfigException(String message) {
        super(message);
    }
}
