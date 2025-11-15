package org.labs.genesis.action.apjwizard.steps;

import org.labs.genesis.action.apjwizard.forms.PageInsertForm;
import org.labs.genesis.config.ApjGenerationContext;

import javax.swing.*;
import java.awt.*;

public class PageInsertWizardStep implements WizardStep {
    private final PageInsertForm pageInsertWizardStep;
    private final ApjGenerationContext context;

    public PageInsertWizardStep(ApjGenerationContext context) {
        this.context = context;
        this.pageInsertWizardStep = new PageInsertForm();
    }

    @Override
    public JComponent getComponent() {
        return pageInsertWizardStep.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "Page Insert";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() {

    }
}
