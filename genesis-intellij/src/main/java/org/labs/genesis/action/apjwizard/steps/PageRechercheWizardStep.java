package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.labs.genesis.action.apjwizard.forms.PageRechercheForm;
import org.labs.genesis.apj.ApjGenerationContext;
import javax.swing.*;
import com.intellij.openapi.project.Project;
import org.labs.genesis.apj.filetype.pages.PageRecherche;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.utils.StringUtils;

public class PageRechercheWizardStep implements WizardStep {
    private final PageRechercheForm pageRechercheForm;
    private final ApjGenerationContext context;
    private final ApjFileGenerator generator = new ApjFileGenerator();
    private final Project project;

    public PageRechercheWizardStep(ApjGenerationContext context,Project project) {
        this.context = context;
        this.pageRechercheForm = new PageRechercheForm(context,project);
        this.project = project;
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
    public void onNext() throws ConfigurationException {
        pageRechercheForm.fillDataTables();
        String fullMapping = pageRechercheForm.getMappingField().getText();
        String mapping = fullMapping;
        int idx = fullMapping.lastIndexOf('.');
        if (idx != -1) {
            mapping = fullMapping.substring(idx + 1);
        }
        String nomTable = pageRechercheForm.getNomTableField().getText();
        String titre = pageRechercheForm.getTitreField().getText();
        PageRecherche pr = (PageRecherche) context.getApjfile();
        pr.setFileName(pageRechercheForm.getNomField().getText());
        pr.setPackageMapping(fullMapping);
        pr.setMapping(mapping);
        pr.setNomTable(nomTable);
        pr.setTitre(titre);
        pr.setApres(pr.getFileName());
        pr.setListeCrt(StringUtils.quoteAndJoin(pageRechercheForm.getListeCrt()));
        pr.setListeInt(StringUtils.quoteAndJoin(pageRechercheForm.getListeInt()));
        pr.setChamps(pageRechercheForm.getDataFiltre());
        pr.setRecap(pageRechercheForm.getDataRecap());
        pr.setTableau(pageRechercheForm.getDataTableau());
        pr.build();
        try {
            generator.generateApjFile(context);
            String fullName = pr.getFileName() + "." + pr.getExtension();
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
