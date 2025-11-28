package org.labs.genesis.config.langage.generator.indicator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenesisProcessIndicator {
    private String message;
    private double progress;

    public void setState(String message, double progress) {
        this.message = message;
        this.progress = progress;
    }
}
