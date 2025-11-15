package org.labs.genesis.action.apjwizard.steps;

import javax.swing.*;

public interface WizardStep {

    String getTitle();

    JComponent getComponent();

    boolean validateStep();

    void onNext();

    default void onBack() {}
}
