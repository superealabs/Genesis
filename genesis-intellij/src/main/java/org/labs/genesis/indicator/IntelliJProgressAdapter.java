package org.labs.genesis.indicator;

import com.intellij.openapi.progress.ProgressIndicator;
import org.labs.genesis.config.langage.generator.indicator.ProgressReporter;

public class IntelliJProgressAdapter implements ProgressReporter {

    private final ProgressIndicator indicator;

    public IntelliJProgressAdapter(ProgressIndicator indicator) {
        this.indicator = indicator;
    }

    @Override
    public void setText(String text) {
        if (indicator != null) {
            indicator.setText(text);
        }
    }

    @Override
    public void setText2(String text) {
        if (indicator != null) {
            indicator.setText2(text);
        }
    }

    @Override
    public void setFraction(double fraction) {
        if (indicator != null) {
            indicator.setFraction(fraction);
        }
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {
        if (indicator != null) {
            indicator.setIndeterminate(indeterminate);
        }
    }

    @Override
    public boolean isCanceled() {
        return indicator != null && indicator.isCanceled();
    }

    @Override
    public boolean isActive() {
        return indicator != null;
    }
}