package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.RuleToCodeForm ;
import javax.swing.*;

public class RuleToCodeWizardStep extends ModuleWizardStep {

    private final GenerationContextManager generationContextManager;
    private final RuleToCodeForm form;
    private final FirstWizardStep initStep ;

    private String pathProject ;
    private Framework framework ;
    private String yamlContent ;

    public RuleToCodeWizardStep(GenerationContextManager generationContextManager , FirstWizardStep initStep) {
        this.form = new RuleToCodeForm();
        this.generationContextManager = generationContextManager;
        this.initStep = initStep;
    }
    private void initializeAttributes(String pathProject , Framework framework , String yamlContent ) {
       this.pathProject = pathProject ;
       this.framework = framework ;
       this.yamlContent = yamlContent ;
    }

    @Override
    public JComponent getComponent() {
        return form.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        // Get name framework selected in combobox
        String selectedFrameworkName = (String) form.getSelectFramework().getSelectedItem();

        Framework selectedFramework = ProjectGenerator.frameworks.values().stream()
                .filter(f -> f.getCoreFramework().equals(selectedFrameworkName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Framework non trouvé : " + selectedFrameworkName));

        initializeAttributes(
                form.getFolderField().getText(),
                selectedFramework,
                form.getYamlContentArea().getText()
        );
        generationContextManager.getContext()
                .setDestinationFolder(pathProject)
                .setFramework(selectedFramework)
                .setProjectDescription(yamlContent);

    }
    @Override
    public boolean validate() throws ConfigurationException {
        if (form.getFolderField().getText().isEmpty()) {
            throw new ConfigurationException("Please select a folder.");
        }
        if (form.getYamlContentArea().getText().isEmpty()) {
            throw new ConfigurationException("YAML Content cannot be empty.");
        }
        return true;
    }

    @Override
    public boolean isStepVisible() {
        return generationContextManager.getContext().getGenerationProcess().isRunToCodeGenerationProcess();
    }
}