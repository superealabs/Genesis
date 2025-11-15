package org.labs.genesis.action.apjwizard.steps;

import org.labs.genesis.action.apjwizard.forms.PropertiesForm;
import org.labs.genesis.config.ApjGenerationContext;

import javax.swing.*;

public class PropertiesWizardStep implements WizardStep {
    private final PropertiesForm propertiesForm;
    private final ApjGenerationContext context;

    public PropertiesWizardStep(ApjGenerationContext context) {
        this.context = context;
        propertiesForm = new PropertiesForm();
    }

    @Override
    public JComponent getComponent() {
        return propertiesForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "Properties";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() {
        String selectedType = (String) propertiesForm.getFileApjType().getSelectedItem();
        context.setApjType(selectedType);
    }


    @Override
    public void onBack() {
        WizardStep.super.onBack();
    }


}
