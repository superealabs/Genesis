package org.labs.genesis.enums;

public enum GenerationProcessChoice {
    GENERATE_NEW_PROJECT(1),
    RULE_TO_CODE_GENERATION(2),
    SYNCHRONISATION(3);

    private final int choiceId;
    private GenerationProcessChoice(int id) {
        this.choiceId = id;
    }
}
