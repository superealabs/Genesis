package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.labs.genesis.action.apjwizard.forms.MappingMereFilleForm;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.mapping.Mapping;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
import org.labs.utils.StringUtils;

import javax.swing.*;
import java.util.ArrayList;

@Getter
public class MappingMereFilleWizardStep implements WizardStep {
    private final MappingMereFilleForm mappingForm;
    private final ApjGenerationContext context;
    private final ApjFileGenerator generator = new ApjFileGenerator();
    private final Project project;

    public MappingMereFilleWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.mappingForm = new MappingMereFilleForm(context,project);
        this.project = project;
    }

    @Override
    public JComponent getComponent() {
        return mappingForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "MappingMereFille";
    }

    @Override
    public boolean validateStep() {
        return true;
    }

    @Override
    public void onNext() throws ConfigurationException {
        mappingForm.fillDataTables();
        String nomTable = mappingForm.getNomTableField().getText();
        String fileName = StringUtils.majStart(mappingForm.getNomField().getText());
        String nomTableFille = mappingForm.getNomTableFilleField().getText();
        String fileNameFille = StringUtils.majStart(mappingForm.getNomFilleField().getText());
        String liaison = mappingForm.getLiaison().getText();
        Mapping mapping = (Mapping) context.getApjfile();
        mapping.setFileName(fileName);
        mapping.setMapping(fileName);
        mapping.setNomTable(nomTable);
        String fullName = mapping.getFileName() + "." + mapping.getExtension();
        String filePath = context.getLocationDir() + "/" + fullName;
        mapping.setPackageMapping(StringUtils.getPackageFromFile(context.getRacineProjet(),filePath));
        mapping.setChamps(mappingForm.getDataForm());
        mapping.setPk(mappingForm.getPrimaryKey());
        mapping.setSuperclasse(ConstantesApj.CLASSMERE);
        mapping.setLiaison(liaison);
        String pack = mapping.getPackageMapping();
        if (pack!=null && !pack.isEmpty()) {
            pack = pack + ".";
        }
        mapping.setClasseLiaison(pack+fileNameFille);
        mapping.setMere(true);
        mapping.setFille(false);
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
        mapping.setPackageImports(new ArrayList<>());
        mapping.setFileName(fileNameFille);
        mapping.setMapping(fileNameFille);
        mapping.setNomTable(nomTableFille);
        fullName = mapping.getFileName() + "." + mapping.getExtension();
        filePath = context.getLocationDir() + "/" + fullName;
        mapping.setPackageMapping(StringUtils.getPackageFromFile(context.getRacineProjet(),filePath));
        mapping.setChamps(mappingForm.getDataFormFille());
        mapping.setPk(mappingForm.getPrimaryKeyFille());
        mapping.setSuperclasse(ConstantesApj.CLASSFILLE);
        mapping.setClasseLiaison(pack+fileName);
        mapping.setMere(false);
        mapping.setFille(true);
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
