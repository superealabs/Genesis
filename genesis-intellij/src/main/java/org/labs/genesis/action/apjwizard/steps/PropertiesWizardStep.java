package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.project.Project;
import org.labs.genesis.action.apjwizard.forms.PropertiesForm;
import org.labs.genesis.config.ApjGenerationContext;
import org.labs.genesis.state.ApjProjectService;
import org.labs.genesis.state.ApjProjectState;

import javax.swing.*;

public class PropertiesWizardStep implements WizardStep {
    private final PropertiesForm propertiesForm;
    private final ApjGenerationContext context;
    private final Project project;

    public PropertiesWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        propertiesForm = new PropertiesForm();
        loadPersistedValues();
    }

    private void loadPersistedValues() {
        if (project == null) return;

        ApjProjectState state = ApjProjectService.getInstance(project).getState();
        if (state == null) return;

        propertiesForm.getLibDir().setText(state.getLibDir());
        propertiesForm.getJarDir().setText(state.getProjectJarDir());
        context.setLibDir(state.getLibDir());
        context.setProjectJarDir(state.getProjectJarDir());
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

    private void saveState() {
        if (project == null) return;

        ApjProjectService service = ApjProjectService.getInstance(project);
        ApjProjectState state = service.getState();
        if (state == null) return;

        state.setLibDir(context.getLibDir());
        state.setProjectJarDir(context.getProjectJarDir());
    }

    @Override
    public void onNext() {
        context.setLibDir(propertiesForm.getLibDir().getText());
        context.setProjectJarDir(propertiesForm.getJarDir().getText());
        context.setApjType((String) propertiesForm.getFileApjType().getSelectedItem());
        saveState();
    }


    @Override
    public void onBack() {
        WizardStep.super.onBack();
    }


}
