package org.labs.genesis.action.apjwizard.steps;

import org.labs.genesis.action.apjwizard.forms.PageRechercheForm;
import org.labs.genesis.apj.ApjGenerationContext;
import javax.swing.*;
import com.intellij.openapi.project.Project;

import java.util.List;

public class PageRechercheWizardStep implements WizardStep {
    private final PageRechercheForm pageRechercheForm;
    private final ApjGenerationContext context;

    public PageRechercheWizardStep(ApjGenerationContext context,Project project) {
        this.context = context;
        this.pageRechercheForm = new PageRechercheForm();
        List<String> tables = null;
        List<String> views = null;
        pageRechercheForm.showClassChooser(project);
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
