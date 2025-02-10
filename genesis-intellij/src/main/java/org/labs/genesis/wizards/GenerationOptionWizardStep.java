package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.forms.GenerationOptionForm;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.labs.genesis.forms.GenerationOptionForm.SELECT_ALL;

public class GenerationOptionWizardStep extends ModuleWizardStep {
    private final GenerationOptionForm generationOptionForm;
    private final ProjectGenerationContext projectGenerationContext;

    public GenerationOptionWizardStep(ProjectGenerationContext projectGenerationContext) {
        this.projectGenerationContext = projectGenerationContext;
        this.generationOptionForm = new GenerationOptionForm(projectGenerationContext);
    }

    @Override
    public JComponent getComponent() {
        return generationOptionForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        try {
            // Obtenir toutes les tables disponibles
            List<String> allTableNames = generationOptionForm.getTableNameStrategy().getTableNames();

            // Obtenir les valeurs sélectionnées depuis l'interface utilisateur
            List<String> selectedValues = generationOptionForm.getTableNamesList().getSelectedValuesList();

            // Gérer la sélection des entités
            List<String> selectedEntities = handleEntitySelection(allTableNames, selectedValues);
            projectGenerationContext.setEntityNames(selectedEntities);

            // Gérer la sélection des composants
            List<String> selectedComponent = generationOptionForm.getComponentChoice().getSelectedValuesList();
            if (selectedComponent != null) {
                projectGenerationContext.setGenerationOptions(selectedComponent);
            }
        } catch (Exception e) {
            Messages.showErrorDialog(
                    generationOptionForm.getMainPanel(),
                    "An error occurred while processing your selections:\n" + e.getMessage(),
                    "Error"
            );
            throw new RuntimeException(e);
        }
    }

    private List<String> handleEntitySelection(List<String> allTableNames, List<String> selectedValues) throws Exception {
        if (allTableNames.isEmpty()) {
            return new ArrayList<>();
        }

        // Vérifier si "*" est sélectionné
        if (selectedValues.contains(SELECT_ALL)) {
            System.out.println("All entities selected.");
            return new ArrayList<>(); // Renvoie une liste vide pour indiquer "tout sélectionner"
        }

        // Valider les entités sélectionnées
        return validateEntitySelection(selectedValues, allTableNames);
    }

    private List<String> validateEntitySelection(List<String> selectedValues, List<String> allTableNames) throws Exception {
        List<String> invalidEntities = new ArrayList<>();
        List<String> validEntities = new ArrayList<>();

        for (String entity : selectedValues) {
            if (allTableNames.contains(entity)) {
                validEntities.add(entity);
            } else {
                invalidEntities.add(entity);
            }
        }

        if (!invalidEntities.isEmpty()) {
            String errorMessage = "The following entities are invalid:\n" + String.join(", ", invalidEntities);
            throw new Exception(errorMessage);
        }

        return validEntities;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        try {
            if (generationOptionForm.getTableNamesList().getSelectedValuesList().isEmpty()) {
                throw new ConfigurationException("Please select at least one table.");
            }
            if (generationOptionForm.getComponentChoice().getSelectedValue() == null) {
                throw new ConfigurationException("Please select a component to generate.");
            }
        } catch (ConfigurationException e) {
            Messages.showErrorDialog(
                    generationOptionForm.getMainPanel(),
                    String.valueOf(e),
                    "Validation Error"
            );
            throw e;
        }
        return true;
    }
}
