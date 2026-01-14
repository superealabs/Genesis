package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.ViewsTemplate;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.FrontendConfigurationForm;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.model.FrontendLayout;
import org.labs.genesis.frontend.generator.model.InterfaceLang;
import org.labs.genesis.frontend.generator.model.ProjectBranding;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontendConfigurationWizardStep extends ModuleWizardStep {
    private final FrontendConfigurationForm frontendConfigurationForm;
    private final List<ProjectGenerationContext> listProjectGenerationContexts;
    private final GenerationContextManager generationContextManager;
    private final FrontendLayout frontendLayout;
    private final ProjectBranding branding;

    public FrontendConfigurationWizardStep(GenerationContextManager generationContextManager,List<ProjectGenerationContext> listProjectGenerationContexts){
        this.frontendConfigurationForm = new FrontendConfigurationForm(listProjectGenerationContexts);
        this.generationContextManager = generationContextManager;
        this.listProjectGenerationContexts = listProjectGenerationContexts ;
        this.frontendLayout = new FrontendLayout();
        this.branding = new ProjectBranding();
        listenerAddFrontendFramework();
    }
    private boolean checkFrontendConfigurationInMultiProject() {
        if(!frontendConfigurationForm.getFrontendGeneration().isSelected()) {
            for (ProjectGenerationContext projectGenerationContext : listProjectGenerationContexts) {
                if (projectGenerationContext.getFrontendFramework() == null) {
                    return false;
                }
            }
        }
        if(frontendConfigurationForm.getFrontendGeneration().isSelected()){
            for (ProjectGenerationContext projectGenerationContext : listProjectGenerationContexts) {
                projectGenerationContext.setFrontendFramework(null);
            }
        }
        return true;
    }
    private void listenerAddFrontendFramework() {
        frontendConfigurationForm.getAddFrontendButton().addActionListener(e -> updateDataModelMulti());
    }
    @Override
    public void updateStep() {
        SwingUtilities.invokeLater(() -> {
            boolean isMultiProject = !listProjectGenerationContexts.isEmpty();
            frontendConfigurationForm.refreshUI(isMultiProject);
        });
    }
    @Override
    public JComponent getComponent() {
        return this.frontendConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        if (!checkFrontendConfigurationInMultiProject()) {
            Messages.showErrorDialog(
                    frontendConfigurationForm.getMainPanel(),
                    "One or more projects don't have a framework frontend.",
                    "Error"
            );
            throw new IllegalArgumentException("Error, one or more projects don't have a framework frontend.");
        }
        generationContextManager.getContext().setFrontendLanguage((FrontendLanguage)frontendConfigurationForm.getFrontendLanguageOptions().getSelectedItem());
        generationContextManager.getContext().setFrontendFramework((FrontendFramework) frontendConfigurationForm.getFrontendFrameworkOptions().getSelectedItem());
        generationContextManager.getContext().setFrontendPort(frontendConfigurationForm.getPortInput().getText().trim());
        updateLayout();
        updateBranding();
        generationContextManager.getContext().getFrontendFramework().setFrontendLayout(this.frontendLayout);
        generationContextManager.getContext().getFrontendFramework().setProjectBranding(this.branding);
        if (generationContextManager.getContext().getFramework() instanceof FrameworkMVC) {
            ((FrameworkMVC) generationContextManager.getContext().getFramework()).setFrontendLayout(this.frontendLayout);
            ((FrameworkMVC) generationContextManager.getContext().getFramework()).setProjectBranding(this.branding);

            if ( generationContextManager.getContext().getFramework().getId() == 6) {
                generationContextManager.getContext().setViewsTemplate(((FrameworkMVC) generationContextManager.getContext().getFramework()).findViewsTemplateById(2));
            } else {
                generationContextManager.getContext().setViewsTemplate(((FrameworkMVC) generationContextManager.getContext().getFramework()).findViewsTemplateById(1));
            }
        }
    }
    public void updateDataModelMulti(){
        try {
            if (multivalidate()) {
                ProjectGenerationContext newProjectGenerationContext = (ProjectGenerationContext) frontendConfigurationForm.getContextList().getSelectedItem();
                newProjectGenerationContext.setFrontendLanguage((FrontendLanguage) frontendConfigurationForm.getFrontendLanguageOptions().getSelectedItem());
                newProjectGenerationContext.setFrontendFramework((FrontendFramework) frontendConfigurationForm.getFrontendFrameworkOptions().getSelectedItem());
                updateLayout();
                updateBranding();
                newProjectGenerationContext.getFrontendFramework().setFrontendLayout(this.frontendLayout);
                newProjectGenerationContext.getFrontendFramework().setProjectBranding(this.branding);
                if (newProjectGenerationContext.getFramework() instanceof FrameworkMVC) {
                    ((FrameworkMVC) newProjectGenerationContext.getFramework()).setFrontendLayout(this.frontendLayout);
                    ((FrameworkMVC) newProjectGenerationContext.getFramework()).setProjectBranding(this.branding);

                    if (newProjectGenerationContext.getFramework().getId() == 6) {
                        newProjectGenerationContext.setViewsTemplate(((FrameworkMVC) newProjectGenerationContext.getFramework()).findViewsTemplateById(2));
                    } else {
                        newProjectGenerationContext.setViewsTemplate(((FrameworkMVC) newProjectGenerationContext.getFramework()).findViewsTemplateById(1));
                    }
                }
                Messages.showInfoMessage(
                        frontendConfigurationForm.getMainPanel(),
                        "Add frontend framework successful!",
                        "Success"
                );
            }
        }catch (Exception e){
            Messages.showErrorDialog(
                    frontendConfigurationForm.getMainPanel(),
                    e.getMessage(),
                    "Error"
            );
            throw new RuntimeException(e);
        }
    }
    public  void updateLayout(){
        this.frontendLayout.setNavbar((String)frontendConfigurationForm.getNavbarSelect().getSelectedItem());
        this.frontendLayout.setPrimaryColor(frontendConfigurationForm.getPrimaryColorField().getText().trim());
        this.frontendLayout.setSecondaryColor(frontendConfigurationForm.getSecondaryColorField().getText().trim());
//        this.frontendLayout.setAdditionalCss(frontendConfigurationForm.getCssTextArea().getText());
        this.frontendLayout.setAdditionalCss("");
        this.frontendLayout.setLangs(frontendConfigurationForm.getInterfaceLangOptions().getSelectedValuesList());
    }

    public void updateBranding(){
        if (!frontendConfigurationForm.getLogoLinkField().getText().isEmpty()){
            this.branding.setLogoLink(frontendConfigurationForm.getLogoLinkField().getText());
            this.branding.setLogoFile(null);
            frontendConfigurationForm.getLogoFileField().setEnabled(false);
        }
        else if (frontendConfigurationForm.getLogoFile() != null){
            this.branding.setLogoLink(null);
            this.branding.setLogoFile(frontendConfigurationForm.getLogoFile());
            frontendConfigurationForm.getLogoFileField().setEnabled(true);
        }

        if (!frontendConfigurationForm.getFaviconLinkField().getText().isEmpty()){
            this.branding.setFaviconLink(frontendConfigurationForm.getFaviconLinkField().getText());
            this.branding.setFaviconFile(null);
            frontendConfigurationForm.getFaviconFileField().setEnabled(false);
        }
        else if (frontendConfigurationForm.getFaviconFile() != null){
            this.branding.setFaviconLink(null);
            this.branding.setFaviconFile(frontendConfigurationForm.getFaviconFile());
            frontendConfigurationForm.getFaviconFileField().setEnabled(true);
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        try {
            if (frontendConfigurationForm.getFrontendGeneration().isSelected()) {
                generationContextManager.getContext().setGenerateFrontendApp(false);
                return  true;
            }
            if(frontendConfigurationForm.getFrontendLanguageOptions().getSelectedItem() == null){
                throw new ConfigurationException("Please select an appropriate frontend language2");
            }
            else if(frontendConfigurationForm.getFrontendFrameworkOptions().getSelectedItem() == null){
                throw new ConfigurationException("Please select a frontend framework to use for generation");
            }
            frontendLayout.isValid();

            // Validations for FrameworkMVC
            Framework framework = generationContextManager.getContext().getFramework();
            if (framework instanceof FrameworkMVC) {
                validateFrameworkMVCConfiguration((FrameworkMVC) framework);
            }

            return true;
        }
        catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }
    public boolean multivalidate() throws ConfigurationException {
        try {
            generationContextManager.getContext().setGenerateFrontendApp(!frontendConfigurationForm.getFrontendGeneration().isSelected());
            if (!generationContextManager.getContext().isGenerateFrontendApp()) {
                return  true;
            }
            generationContextManager.getContext().setGenerateFrontendApp(true);
            if(frontendConfigurationForm.getFrontendLanguageOptions().getSelectedItem() == null){
                throw new ConfigurationException("Please select an appropriate frontend language");
            }
            else if(frontendConfigurationForm.getFrontendFrameworkOptions().getSelectedItem() == null){
                throw new ConfigurationException("Please select a frontend framework to use for generation");
            }
            Framework framework = generationContextManager.getContext().getFramework();
            if (framework instanceof FrameworkMVC) {
                validateFrameworkMVCConfiguration((FrameworkMVC) framework);
            }

            return true;
        }
        catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    private void validateFrameworkMVCConfiguration(FrameworkMVC frameworkMVC) throws ConfigurationException {
        if (frameworkMVC == null) {
            throw new ConfigurationException("Framework must not be null");
        }
    }

    public void onFrameworkMVCSelected(FrameworkMVC frameworkMVC) {
        if (frameworkMVC == null) {
            throw new IllegalArgumentException("Framework must not be null");
        }
        frontendConfigurationForm.updateFormWithFrameworkMVCOptions(frameworkMVC);
    }

    public void onFrameworkSelected() {
        frontendConfigurationForm.updateFormWithFrontendFrameworkOptions();
    }

    @Override
    public boolean isStepVisible() {
        return this.generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess();
    }
}
