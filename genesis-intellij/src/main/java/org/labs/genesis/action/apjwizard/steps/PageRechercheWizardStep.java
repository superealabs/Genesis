package org.labs.genesis.action.apjwizard.steps;

import org.labs.genesis.action.apjwizard.forms.PageRechercheForm;
import org.labs.genesis.apj.ApjGenerationContext;
import javax.swing.*;
import com.intellij.openapi.project.Project;

public class PageRechercheWizardStep implements WizardStep {
    private final PageRechercheForm pageRechercheForm;
    private final ApjGenerationContext context;

    public PageRechercheWizardStep(ApjGenerationContext context,Project project) {
        this.context = context;
        this.pageRechercheForm = new PageRechercheForm();
        String[] tables = context.getTables();
        String[] views = context.getVues();
        pageRechercheForm.showClassChooser(project,context);
        pageRechercheForm.getChooseTableButton().addActionListener(e -> pageRechercheForm.showTableTree(tables, views));
    }

    @Override
    public JComponent getComponent() {
        return pageRechercheForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "PageRecherche";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() {

    }
}
