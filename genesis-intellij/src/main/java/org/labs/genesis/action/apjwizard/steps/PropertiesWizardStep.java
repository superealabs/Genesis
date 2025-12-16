package org.labs.genesis.action.apjwizard.steps;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.labs.genesis.action.apjwizard.forms.PropertiesForm;
import org.labs.genesis.action.apjwizard.forms.helper.ProgressUtils;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.state.ApjProjectService;
import org.labs.genesis.state.ApjProjectState;

import javax.swing.*;
import java.sql.Connection;

public class PropertiesWizardStep implements WizardStep {
    private final PropertiesForm propertiesForm;
    private final ApjGenerationContext context;
    private final Project project;

    public PropertiesWizardStep(ApjGenerationContext context, Project project) {
        this.context = context;
        this.project = project;
        propertiesForm = new PropertiesForm();
        loadStateAndInitializeForm();
    }

    private void loadStateAndInitializeForm() {
        if (project == null) return;
        propertiesForm.setupFolderChoosers(project);
        propertiesForm.addTestConnectionButtonListener(project);

        ApjProjectState state = ApjProjectService.getInstance(project).getState();
        if (state == null) return;
        propertiesForm.getLibDir().setText(state.getLibDir());
        propertiesForm.getJarDir().setText(state.getProjectJarDir());
        propertiesForm.getRacineProjetField().setText(state.getRacineProjet());
        propertiesForm.getRacinePageField().setText(state.getRacinePage());
        propertiesForm.getLocation().setText(context.getLocationDir());
        context.setLibDir(state.getLibDir());
        context.setProjectJarDir(state.getProjectJarDir());
        context.setRacinePage(state.getRacinePage());
        context.setRacineProjet(state.getRacineProjet());
    }

    @Override
    public JComponent getComponent() {
        return propertiesForm.getMainPanel();
    }

    @Override
    public String getTitle() {
        return "Propriétés";
    }

    private void saveState() {
        if (project == null) return;

        ApjProjectService service = ApjProjectService.getInstance(project);
        ApjProjectState state = service.getState();
        if (state == null) return;

        state.setLibDir(context.getLibDir());
        state.setProjectJarDir(context.getProjectJarDir());
        state.setRacineProjet(context.getRacineProjet());
        state.setRacinePage(context.getRacinePage());
    }

    @Override
    public void onNext() throws ConfigurationException {
        String jarDir = propertiesForm.getJarDir().getText();
        String libDir = propertiesForm.getLibDir().getText();
        String racinePage = propertiesForm.getRacinePageField().getText();
        String racineProjet = propertiesForm.getRacineProjetField().getText();
        context.setLocationDir(propertiesForm.getLocation().getText());
        context.setLibDir(libDir);
        context.setProjectJarDir(jarDir);
        context.setRacinePage(racinePage);
        context.setRacineProjet(racineProjet);
        context.setApjfile((ApjFile) propertiesForm.getFileApjOptions().getSelectedItem());
        saveState();
        boolean sansBase = propertiesForm.getSansBaseCheckBox().isSelected();
        if (sansBase) {
            return;
        };

        ProgressUtils.runWithProgress(project, "Chargement des tables et vues…", indicator -> {
            try (Connection conn = UtilDBDynamique.GetConn(jarDir, libDir)) {

                ProgressUtils.updateProgress(indicator, "Chargement des tables...", 0.5);
                String[] tables = UtilDBDynamique.getTablesOrViews(conn, false);

                ProgressUtils.updateProgress(indicator, "Chargement des vues...", 0.9);
                String[] vues = UtilDBDynamique.getTablesOrViews(conn, true);

                context.setTables(tables);
                context.setVues(vues);

                ProgressUtils.updateProgress(indicator, "Terminé", 1.0);

            } catch (Exception e) {
                throw new ConfigurationException("Échec de la connexion: " + e.getMessage());
            }
        });
    }


    @Override
    public void onBack() {
        WizardStep.super.onBack();
    }

    @Override
    public boolean validateStep() throws ConfigurationException {
        String location = propertiesForm.getLocation().getText();
        String libDir = propertiesForm.getLibDir().getText();
        String jarDir = propertiesForm.getJarDir().getText();
        if (location.isEmpty()) throw new ConfigurationException("The location path cannot be empty.");
        if (libDir.isEmpty()) throw new ConfigurationException("The lib path cannot be empty.");
        if (jarDir.isEmpty()) throw new ConfigurationException("The project jar path cannot be empty.");

        boolean sansBase = propertiesForm.getSansBaseCheckBox().isSelected();
        if (sansBase) {
            return true;
        };
        ProgressUtils.runWithProgress(project, "Validation de la connexion à la base de données…", indicator -> {
            try (Connection conn = UtilDBDynamique.GetConn(jarDir, libDir)) {
                ProgressUtils.updateProgress(indicator, "Connexion réussie!", 1.0);
            } catch (Exception e) {
                throw new ConfigurationException("Échec de la connexion: " + e.getMessage());
            }
        });
        return true;
    }


}
