package org.labs.genesis.api.exception;

public class GenerationAlreadyRunningException extends RuntimeException {

    public GenerationAlreadyRunningException() {
        super("Une génération Genesis est déjà en cours");
    }
}