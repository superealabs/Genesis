package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.labs.genesis.action.apjwizard.forms.MappingForm;
import org.labs.genesis.action.apjwizard.forms.MappingMereFilleForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.mapping.Mapping;
import org.labs.genesis.apj.filetype.pages.PageConsulte;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.utils.StringUtils;

import javax.swing.*;

@Getter
public class MappingWizardStep implements WizardStep {
    private final MappingForm mappingForm;
    private final ApjGenerationContext context;
    private final ApjFileGenerator generator = new ApjFileGenerator();
    private final Project project;

    public MappingWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.mappingForm = new MappingForm(context,project);
        this.project = project;
    }

    @Override
    public JComponent getComponent() {
        return mappingForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "Mapping";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() throws ConfigurationException {
        mappingForm.fillDataTables();
        String nomTable = mappingForm.getNomTableField().getText();
        String fileName = mappingForm.getNomField().getText();
        String indicePK = mappingForm.getIndicePK().getText();
        String pSeq = mappingForm.getPSeq().getText();
        Mapping mapping = (Mapping) context.getApjfile();
        mapping.setFileName(mappingForm.getNomField().getText());
        mapping.setMapping(fileName);
        mapping.setNomTable(nomTable);
        mapping.setIndicePK(indicePK);
        mapping.setPSeq(pSeq);
        String fullName = mapping.getFileName() + "." + mapping.getExtension();
        String filePath = context.getLocationDir() + "/" + fullName;
        mapping.setPackageMapping(StringUtils.getPackageFromFile(context.getRacineProjet(),filePath));
        mapping.setChamps(mappingForm.getDataForm());
        mapping.setPk(mappingForm.getPrimaryKey());
        mapping.setSuperclasse(String.valueOf(mappingForm.getSuperClassComboBox().getSelectedItem()));
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
