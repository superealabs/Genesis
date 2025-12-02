package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.forms.GenerationOptionForm;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.labs.genesis.forms.GenerationOptionForm.SELECT_ALL;

public class GenerationOptionWizardStep extends ModuleWizardStep {
    private final GenerationOptionForm generationOptionForm;
    private final ProjectGenerationContext projectGenerationContext;
    private final List<ProjectGenerationContext> listProjectGenerationContexts;
    public final SpecificConfigurationWizardStep specificConfigurationWizardStep;

    public GenerationOptionWizardStep(ProjectGenerationContext projectGenerationContext,List<ProjectGenerationContext> listProjectGenerationContexts ,SpecificConfigurationWizardStep specificConfigurationWizardStep) {
        this.projectGenerationContext = projectGenerationContext;
        this.listProjectGenerationContexts = listProjectGenerationContexts ;
        this.generationOptionForm = new GenerationOptionForm(projectGenerationContext , listProjectGenerationContexts);
        this.specificConfigurationWizardStep = specificConfigurationWizardStep;
        listenerAddGenerationOption();
    }
    private boolean checkGenerationInMultiProject() {
        for (ProjectGenerationContext projectGenerationContext : listProjectGenerationContexts) {
            if ( projectGenerationContext.getGenerationOptions() == null)
            { return false; }
        }
        return true;
    }
    private void listenerAddGenerationOption() {
        generationOptionForm.getAddGenerationButton().addActionListener(e -> updateDataModelMulti());
    }
    @Override
    public void updateStep() {
        SwingUtilities.invokeLater(() -> {
            boolean isMultiProject = !listProjectGenerationContexts.isEmpty();
            generationOptionForm.refreshUI(isMultiProject);
        });
    }
    @Override
    public boolean isStepVisible() {
        Framework framework = projectGenerationContext.getFramework();
        return framework != null && framework.getUseDB();
    }
    @Override
    public JComponent getComponent() {
        if (isStepVisible()) {
            return generationOptionForm.getMainPanel();
        } else {
            return new JLabel("This step is not visible");
        }
    }

    @Override
    public void updateDataModel() {
        try {
            // Obtenir toutes les tables disponibles
            List<String> allTableNames = generationOptionForm.getAllTableNames();

            // Obtenir toutes les vues disponibles
            List<String> allViewNames = generationOptionForm.getAllViewsNames();

            // Obtenir les valeurs sélectionnées depuis l'interface utilisateur
            List<String> selectedValues = generationOptionForm.getTableNamesList().getSelectedValuesList();

            // Obtenir les vues sélectionnées depuis l'interface utilisateur
            List<String> selectedViewValues = generationOptionForm.getViewNamesList().getSelectedValuesList();

            // Gérer la sélection des entités
            List<String> selectedEntities = handleEntitySelection(allTableNames, selectedValues);
            projectGenerationContext.setEntityNames(selectedEntities);

            // Gérer la sélection des vues
            List<String> selectedViews = handleEntitySelection(allViewNames, selectedViewValues);
            projectGenerationContext.setViewNames(selectedViews);

            // Gérer la sélection des composants
            List<String> selectedComponent = generationOptionForm.getComponentChoice().getSelectedValuesList();
            if (selectedComponent != null) {
                projectGenerationContext.setGenerationOptions(selectedComponent);
            }
            if (!checkGenerationInMultiProject()) {
                throw new IllegalArgumentException("Error, one or more projects don't have option.");
            }
            specificConfigurationWizardStep.onTablesAndViewsSelected(handleSelectAll(selectedValues, generationOptionForm.getAllTableNames()), handleSelectAll(selectedViewValues, generationOptionForm.getAllViewsNames()));
        } catch (Exception e) {
            Messages.showErrorDialog(
                    generationOptionForm.getMainPanel(),
                    "An error occurred while processing your selections:\n" + e.getMessage(),
                    "Error"
            );
            throw new RuntimeException(e);
        }
    }
    private boolean checkGenerationOptionsMulti(List<String> tableNamesForm, List<String> tableNamesDatabase) {
        if (tableNamesForm.size() != tableNamesDatabase.size()) {
            return false;
        }
        for (int i = 0; i < tableNamesForm.size(); i++) {
            if (!tableNamesForm.get(i).equals(tableNamesDatabase.get(i))) {
                return false;
            }
        }
        return true;
    }
    private List<String> castJBlist( JBList<String> jbList){
        List<String> tableNames = new ArrayList<>();
        ListModel<String> model = jbList.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            tableNames.add(model.getElementAt(i));
        }
        return tableNames;
    }

    public void updateDataModelMulti(){
        try {
            if(multivalidate()) {
                ProjectGenerationContext newProjectGenerationContext = (ProjectGenerationContext) generationOptionForm.getContextList().getSelectedItem();
                // Obtenir toutes les tables disponibles
                List<String> allTableNames = generationOptionForm.getAllTableNames();

                // Obtenir toutes les vues disponibles
                List<String> allViewNames = generationOptionForm.getAllViewsNames();

                JBList<String> jbList = generationOptionForm.getTableNamesList();
                List<String> tableNamesForm = castJBlist(jbList);
                List<String> allTableNamesDatabase = generationOptionForm.getAllTableNames(newProjectGenerationContext);
                if(!checkGenerationOptionsMulti(  tableNamesForm, allTableNamesDatabase)){
                    throw new IllegalArgumentException("Error: the list of tables and views does not match the project's database.");
                }

                // Obtenir les valeurs sélectionnées depuis l'interface utilisateur
                List<String> selectedValues = generationOptionForm.getTableNamesList().getSelectedValuesList();

                // Obtenir les vues sélectionnées depuis l'interface utilisateur
                List<String> selectedViewValues = generationOptionForm.getViewNamesList().getSelectedValuesList();

                // Gérer la sélection des entités
                List<String> selectedEntities = handleEntitySelection(allTableNames, selectedValues);
                newProjectGenerationContext.setEntityNames(selectedEntities);

                // Gérer la sélection des vues
                List<String> selectedViews = handleEntitySelection(allViewNames, selectedViewValues);
                newProjectGenerationContext.setViewNames(selectedViews);

                // Gérer la sélection des composants
                List<String> selectedComponent = generationOptionForm.getComponentChoice().getSelectedValuesList();
                if (selectedComponent != null) {
                    newProjectGenerationContext.setGenerationOptions(selectedComponent);
                }

                specificConfigurationWizardStep.onTablesAndViewsSelected(handleSelectAll(selectedValues, generationOptionForm.getAllTableNames()), handleSelectAll(selectedViewValues, generationOptionForm.getAllViewsNames()));
                Messages.showInfoMessage(
                        generationOptionForm.getMainPanel(),
                        "Add components successful!",
                        "Success"
                );
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

    private List<String> handleSelectAll(List<String> selectedValues, List<String> allValues) {
        if (selectedValues.contains(SELECT_ALL)) {
            List<String> result = new ArrayList<>(allValues);
            result.remove(SELECT_ALL);
            return result;
        }
        return selectedValues;
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
    public boolean multivalidate() throws ConfigurationException {
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
