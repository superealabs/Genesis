package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.labs.genesis.action.apjwizard.forms.PageInsertMultipleForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.pages.PageInsertMultiple;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.utils.StringUtils;

import javax.swing.*;

public class PageInsertMultipleWizardStep implements WizardStep {
    private final PageInsertMultipleForm pageInsertMultipleForm;
    private final ApjGenerationContext context;
    private final ApjFileGenerator generator = new ApjFileGenerator();
    private final Project project;

    public PageInsertMultipleWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        this.pageInsertMultipleForm = new PageInsertMultipleForm(context,project);
        String[] tables = context.getTables();
        String[] views = context.getVues();
        pageInsertMultipleForm.showClassChooser(project,context);
        pageInsertMultipleForm.getChooseTableButton().addActionListener(e -> pageInsertMultipleForm.showTableTree(tables, views));
        pageInsertMultipleForm.getChooseTableFilleButton().addActionListener(e -> pageInsertMultipleForm.showTableTree(tables, views));
    }

    @Override
    public JComponent getComponent() {
        return pageInsertMultipleForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "PageInsertMultiple";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() throws ConfigurationException {
        pageInsertMultipleForm.fillDataTables();
        String fullMapping = pageInsertMultipleForm.getMappingField().getText();
        String fullMappingFille = pageInsertMultipleForm.getMappingFilleField().getText();
        String mapping = fullMapping;
        String mappingFille = fullMappingFille;
        int idx = fullMapping.lastIndexOf('.');
        int idxFille = fullMappingFille.lastIndexOf('.');
        if (idx != -1) {
            mapping = fullMapping.substring(idx + 1);
        }
        if (idxFille != -1) {
            mappingFille = fullMappingFille.substring(idxFille + 1);
        }
        String nomTable = pageInsertMultipleForm.getNomTableField().getText();
        String nomTableFille = pageInsertMultipleForm.getNomTableFilleField().getText();
        String colonneMere = pageInsertMultipleForm.getColonneMereField().getText();
        String titre = pageInsertMultipleForm.getTitreField().getText();
        String titreUpdate = pageInsertMultipleForm.getTitreUpdateField().getText();
        PageInsertMultiple pi = (PageInsertMultiple) context.getApjfile();
        pi.setFileName(pageInsertMultipleForm.getNomField().getText());
        pi.setPackageMapping(fullMapping);
        pi.setPackageMappingFille(fullMappingFille);
        pi.setMapping(mapping);
        pi.setNomTable(nomTable);
        pi.setNomTableFille(nomTableFille);
        pi.setMappingFille(mappingFille);
        pi.setColonneMere(colonneMere);
        pi.setTitre(titre);
        pi.setTitreUpdate(titreUpdate);
        String fullName = pi.getFileName() + "." + pi.getExtension();
        String filePath = context.getLocationDir() + "/" + fullName;
        pi.setApres(StringUtils.relativeOrFilename(context.getRacinePage(),filePath));
        pi.setChamps(pageInsertMultipleForm.getDataForm());
        pi.setChampsFille(pageInsertMultipleForm.getDataFormFille());
        pi.build();
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
