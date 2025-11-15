package org.labs.genesis.action.apjwizard;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import org.labs.genesis.action.apjwizard.steps.PropertiesWizardStep;
import org.labs.genesis.config.ApjGenerationContext;

import javax.swing.*;
import java.awt.*;

public class ApjWizardDialog extends DialogWrapper {
    private JPanel mainPanel;
    private final PropertiesWizardStep step;

    public ApjWizardDialog() {
        super(true);
        step = new PropertiesWizardStep(new ApjGenerationContext());
        init();
    }


    @Override
    protected @Nullable JComponent createCenterPanel() {
        return step.getComponent();
    }
}
