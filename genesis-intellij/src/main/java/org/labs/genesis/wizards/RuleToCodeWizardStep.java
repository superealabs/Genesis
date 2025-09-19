package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.forms.RuleToCodeForm ;
import javax.swing.*;

public class RuleToCodeWizardStep extends ModuleWizardStep {

    private final ProjectGenerationContext context;
    private final RuleToCodeForm form;
    private final FirstWizardStep initStep ;

    private String pathProject ;
    private Framework framework ;
    private String yamlContent ;

    public RuleToCodeWizardStep(ProjectGenerationContext context , FirstWizardStep initStep) {
        this.form = new RuleToCodeForm();
        this.context = context;
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
        initializeAttributes(
                form.getFolderField().getText() ,
                (Framework) form.getFrameworkOptions().getSelectedItem() ,
                form.getYamlContentArea().getText()
        );
        context
                .setDestinationFolder(pathProject)
                .setFramework(framework)
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
       return initStep.getFirstForm().ruleTodCodeSelected();
    }
}