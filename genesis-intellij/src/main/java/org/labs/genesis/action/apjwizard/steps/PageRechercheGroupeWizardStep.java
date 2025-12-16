package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.labs.genesis.action.apjwizard.forms.PageRechercheGroupeForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.pages.PageRechercheGroupe;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.utils.StringUtils;

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
        pageRechercheGroupeForm.fillDataTables();
        String fullMapping = pageRechercheGroupeForm.getMappingField().getText();
        String mapping = fullMapping;
        int idx = fullMapping.lastIndexOf('.');
        if (idx != -1) {
            mapping = fullMapping.substring(idx + 1);
        }
        String nomTable = pageRechercheGroupeForm.getNomTableField().getText();
        String titre = pageRechercheGroupeForm.getTitreField().getText();
        String lienColGr = pageRechercheGroupeForm.getColGrColLien().getText();
        String colGr =  String.valueOf(pageRechercheGroupeForm.getColGrField().getSelectedItem());
        String colGrCol =  String.valueOf(pageRechercheGroupeForm.getColGrColField().getSelectedItem());
        PageRechercheGroupe pr = (PageRechercheGroupe) context.getApjfile();
        pr.setFileName(pageRechercheGroupeForm.getNomField().getText());
        pr.setPackageMapping(fullMapping);
        pr.setMapping(mapping);
        pr.setNomTable(nomTable);
        pr.setTitre(titre);
        String fullName = pr.getFileName() + "." + pr.getExtension();
        String filePath = context.getLocationDir() + "/" + fullName;
        pr.setApres(StringUtils.relativeOrFilename(context.getRacinePage(),filePath));
        pr.setListeCrt(StringUtils.quoteAndJoin(pageRechercheGroupeForm.getListeCrt()));
        pr.setListeInt(StringUtils.quoteAndJoin(pageRechercheGroupeForm.getListeInt()));
        pr.setLienColGrCol(lienColGr);
        pr.setColGr(colGr);
        pr.setColGrCol(colGrCol);
        pr.setChamps(pageRechercheGroupeForm.getDataFiltre());
        pr.setRecap(pageRechercheGroupeForm.getDataRecap());
        pr.build();
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
