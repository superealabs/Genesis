package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.labs.genesis.action.apjwizard.forms.PageInsertForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.pages.PageInsert;
import org.labs.genesis.apj.generator.ApjFileGenerator;


import javax.swing.*;

public class PageInsertWizardStep implements WizardStep {
    private final PageInsertForm pageInsertForm;
    private final ApjGenerationContext context;
    private final ApjFileGenerator generator = new ApjFileGenerator();
    private final Project project;

    public PageInsertWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        this.pageInsertForm = new PageInsertForm();
        String[] tables = context.getTables();
        String[] views = context.getVues();
        pageInsertForm.showClassChooser(project,context);
        pageInsertForm.getChooseTableButton().addActionListener(e -> pageInsertForm.showTableTree(tables, views));
    }

    @Override
    public JComponent getComponent() {
        return pageInsertForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "PageInsert";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() throws ConfigurationException {
        pageInsertForm.fillDataTables();
        String fullMapping = pageInsertForm.getMappingField().getText();
        String mapping = fullMapping;
        int idx = fullMapping.lastIndexOf('.');
        if (idx != -1) {
            mapping = fullMapping.substring(idx + 1);
        }
        String nomTable = pageInsertForm.getNomTableField().getText();
        String titre = pageInsertForm.getTitreField().getText();
        String titreUpdate = pageInsertForm.getTitreUpdateField().getText();
        PageInsert pi = (PageInsert) context.getApjfile();
        pi.setFileName(pageInsertForm.getNomField().getText());
        pi.setPackageMapping(fullMapping);
        pi.setMapping(mapping);
        pi.setNomTable(nomTable);
        pi.setTitre(titre);
        pi.setTitreUpdate(titreUpdate);
        pi.setApres(pi.getFileName());
        pi.setChamps(pageInsertForm.getDataForm());
        try {
            generator.generateApjFile(context);
            String fullName = pi.getFileName() + "." + pi.getExtension();
            String filePath = context.getLocationDir() + "/" + fullName;

            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath);
            if (vf != null) {
                FileEditorManager.getInstance(this.project).openFile(vf, true);
            } else {
                throw new ConfigurationException("File not found: " + filePath);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Generation failed : " + e.getMessage());
        }
    }
}
