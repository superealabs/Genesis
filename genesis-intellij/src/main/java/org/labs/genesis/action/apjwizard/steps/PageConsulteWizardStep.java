package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.labs.genesis.action.apjwizard.forms.PageConsulteForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.pages.PageConsulte;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.utils.StringUtils;

import javax.swing.*;

public class PageConsulteWizardStep implements WizardStep {
    private final PageConsulteForm pageConsulteForm;
    private final ApjGenerationContext context;
    private final ApjFileGenerator generator = new ApjFileGenerator();
    private final Project project;

    public PageConsulteWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        this.pageConsulteForm = new PageConsulteForm();
        String[] tables = context.getTables();
        String[] views = context.getVues();
        pageConsulteForm.showClassChooser(project,context);
        pageConsulteForm.getChooseTableButton().addActionListener(e -> pageConsulteForm.showTableTree(tables, views));
    }

    @Override
    public JComponent getComponent() {
        return pageConsulteForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "PageConsulte";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() throws ConfigurationException {
        pageConsulteForm.fillDataTables();
        String fullMapping = pageConsulteForm.getMappingField().getText();
        String mapping = fullMapping;
        int idx = fullMapping.lastIndexOf('.');
        if (idx != -1) {
            mapping = fullMapping.substring(idx + 1);
        }
        String nomTable = pageConsulteForm.getNomTableField().getText();
        String titre = pageConsulteForm.getTitreField().getText();
        String pageApresDelete = pageConsulteForm.getPageApresDelete().getText();
        String pageModif = pageConsulteForm.getPageModifField().getText();
        String pageRetour = pageConsulteForm.getPageRetourField().getText();
        boolean withOnglet = pageConsulteForm.getWithOngletCheckBox().isSelected();
        PageConsulte pc = (PageConsulte) context.getApjfile();
        pc.setFileName(pageConsulteForm.getNomField().getText());
        pc.setPackageMapping(fullMapping);
        pc.setMapping(mapping);
        pc.setNomTable(nomTable);
        pc.setTitre(titre);
        String fullName = pc.getFileName() + "." + pc.getExtension();
        String filePath = context.getLocationDir() + "/" + fullName;
        pc.setPageActuel(StringUtils.relativeOrFilename(context.getRacinePage(),filePath));
        pc.setPageModif(pageModif);
        pc.setPageRetour(pageRetour);
        pc.setPageApresDelete(pageApresDelete);
        pc.setWithOnglet(withOnglet);
        pc.setChamps(pageConsulteForm.getDataForm());
        try {
            generator.generateApjFile(context);

            VirtualFile vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath);
            if (vf != null) {
                FileEditorManager.getInstance(this.project).openFile(vf, true);
            } else {
                throw new ConfigurationException("Fichier introuvable : " + filePath);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Échec de la génération : " + e.getMessage());
        }
    }
}
