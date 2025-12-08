package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.labs.genesis.action.apjwizard.forms.PageRechercheGroupeForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import javax.swing.*;

@Getter
public class PageRechercheGroupeWizardStep implements WizardStep {
    private final PageRechercheGroupeForm pageRechercheGroupeForm;
    private final ApjGenerationContext context;
    private final ApjFileGenerator generator = new ApjFileGenerator();
    private final Project project;

    public PageRechercheGroupeWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.pageRechercheGroupeForm = new PageRechercheGroupeForm(context,project);
        this.project = project;
        String[] tables = context.getTables();
        String[] views = context.getVues();
        pageRechercheGroupeForm.showClassChooser(project,context);
        pageRechercheGroupeForm.getChooseTableButton().addActionListener(e -> pageRechercheGroupeForm.showTableTree(tables, views));
    }

    @Override
    public JComponent getComponent() {
        return pageRechercheGroupeForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "PageRechercheGroupe";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() throws ConfigurationException {

    }
}
