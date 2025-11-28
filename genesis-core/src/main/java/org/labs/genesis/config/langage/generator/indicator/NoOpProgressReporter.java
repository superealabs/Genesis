package org.labs.genesis.config.langage.generator.indicator;

public class NoOpProgressReporter implements ProgressReporter {

    @Override
    public void setText(String text) {
        // No-op
    }

    @Override
    public void setText2(String text) {
        // No-op
    }

    @Override
    public void setFraction(double fraction) {
        // No-op
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {
        // No-op
    }

    @Override
    public boolean isCanceled() {
        return false;
    }
}