package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.ViewsTemplate;
import org.labs.genesis.config.langage.ViewsTemplateEngine;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
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
    private final ProjectGenerationContext projectGenerationContext;
    private final FrontendLayout frontendLayout;
    private final ProjectBranding branding;

    public FrontendConfigurationWizardStep(ProjectGenerationContext projectGenerationContext){
        this.frontendConfigurationForm = new FrontendConfigurationForm();
        this.projectGenerationContext = projectGenerationContext;
        this.frontendLayout = new FrontendLayout();
        this.branding = new ProjectBranding();
    }

    @Override
    public JComponent getComponent() {
        return this.frontendConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        projectGenerationContext.setFrontendLanguage((FrontendLanguage)frontendConfigurationForm.getFrontendLanguageOptions().getSelectedItem());
        projectGenerationContext.setFrontendFramework((FrontendFramework) frontendConfigurationForm.getFrontendFrameworkOptions().getSelectedItem());
        projectGenerationContext.getFrontendFramework().setFrontendLayout(this.frontendLayout);
        projectGenerationContext.getFrontendFramework().setProjectBranding(this.branding);
        if (projectGenerationContext.getFramework() instanceof FrameworkMVC) {
            ((FrameworkMVC) projectGenerationContext.getFramework()).setFrontendLayout(this.frontendLayout);
            ((FrameworkMVC) projectGenerationContext.getFramework()).setProjectBranding(this.branding);
            projectGenerationContext.setViewsTemplateEngine((ViewsTemplateEngine) frontendConfigurationForm.getViewsTemplateEngineOptions().getSelectedItem());
            projectGenerationContext.setViewsTemplate(((FrameworkMVC) projectGenerationContext.getFramework()).findViewsTemplateById(1));
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        try {

            if(frontendConfigurationForm.getFrontendLanguageOptions().getSelectedItem() == null){
                throw new ConfigurationException("Please select an appropriate frontend language");
            }
            else if(frontendConfigurationForm.getFrontendFrameworkOptions().getSelectedItem() == null){
                throw new ConfigurationException("Please select a frontend framework to use for generation");
            }

            // Update Layout
            this.frontendLayout.setNavbar((String)frontendConfigurationForm.getNavbarSelect().getSelectedItem());
            this.frontendLayout.setPrimaryColor(frontendConfigurationForm.getPrimaryColorField().getText());
            this.frontendLayout.setSecondaryColor(frontendConfigurationForm.getSecondaryColorField().getText());
            this.frontendLayout.setAdditionalCss(frontendConfigurationForm.getCssTextArea().getText());
            List<InterfaceLang> langs = frontendConfigurationForm.getInterfaceLangOptions().getSelectedValuesList();
            if (langs.size() < 0) {
                InterfaceLang defaultLang = ProjectGenerator.langs.get(1);
                frontendLayout.setLangs( new ArrayList<>());
                frontendLayout.getLangs().add(defaultLang);
            } else  {
                this.frontendLayout.setLangs(frontendConfigurationForm.getInterfaceLangOptions().getSelectedValuesList());
            }
            frontendLayout.isValid();
            // Update branding
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

            // Validations for FrameworkMVC
            Framework framework = projectGenerationContext.getFramework();
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
        ViewsTemplateEngine viewsTemplateEngine = (ViewsTemplateEngine) frontendConfigurationForm.getViewsTemplateEngineOptions().getSelectedItem();
        if (viewsTemplateEngine == null) {
            throw new ConfigurationException("Template Engine must not be null");
        }
        if (viewsTemplateEngine.getFrameworkMvcId() != frameworkMVC.getId()) {
            throw new ConfigurationException("Template Engine not compatible for " + frameworkMVC.getName());
        }
    }

    public void onFrameworkMVCSelected(FrameworkMVC frameworkMVC) {
        if (frameworkMVC == null) {
            throw new IllegalArgumentException("Framework must not be null");
        }
        frontendConfigurationForm.updateFormWithFrameworkMVCOptions(frameworkMVC);
    }
}
