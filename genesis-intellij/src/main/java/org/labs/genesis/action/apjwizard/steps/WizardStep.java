package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

public interface WizardStep {

    String getTitle();

    JComponent getComponent();

    boolean validateStep() throws ConfigurationException;

    void onNext();

    default void onBack() {}
}
