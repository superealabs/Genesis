package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.labs.genesis.action.apjwizard.forms.PropertiesForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.state.ApjProjectService;
import org.labs.genesis.state.ApjProjectState;

import javax.swing.*;
import java.sql.Connection;

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
        propertiesForm.getLocation().setText(context.getLocationDir());
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
        context.setLocationDir(propertiesForm.getLocation().getText());
        context.setLibDir(propertiesForm.getLibDir().getText());
        context.setProjectJarDir(propertiesForm.getJarDir().getText());
        context.setApjfile((ApjFile) propertiesForm.getFileApjOptions().getSelectedItem());
        saveState();
    }

    @Override
    public void onBack() {
        WizardStep.super.onBack();
    }

    @Override
    public boolean validateStep() throws ConfigurationException {
        String location = propertiesForm.getLocation().getText();
        String libDir = propertiesForm.getLibDir().getText();
        String jarDir = propertiesForm.getJarDir().getText();
        if (location.isEmpty()) {
            throw new ConfigurationException("The location path cannot be empty.");
        }
        if (libDir.isEmpty()) {
            throw new ConfigurationException("The lib path cannot be empty.");
        }
        if (jarDir.isEmpty()) {
            throw new ConfigurationException("The project jar path cannot be empty.");
        }
        try (Connection conn = UtilDBDynamique.GetConn(jarDir,libDir)) {
            // Connection successful, nothing else to do
        } catch (Exception e) {
            throw new ConfigurationException("Connection failed: " + e.getMessage());
        }
        return true;
    }


}
