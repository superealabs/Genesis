package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.DatabaseConfigurationForm;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.labs.genesis.Utils.formatErrorMessageHtml;

public class DatabaseConfigurationWizardStep extends ModuleWizardStep {
    private final DatabaseConfigurationForm databaseConfigurationForm;
    private final GenerationContextManager generationContextManager;
    private final List<ProjectGenerationContext> listProjectGenerationContexts;
    private boolean ignoreUpdates = false;

    public DatabaseConfigurationWizardStep(GenerationContextManager generationContextManager, List<ProjectGenerationContext> listProjectGenerationContexts) {
        this.listProjectGenerationContexts = listProjectGenerationContexts;
        databaseConfigurationForm = new DatabaseConfigurationForm(listProjectGenerationContexts);
        this.generationContextManager = generationContextManager;
        listenerAddBase();
    }
    private boolean checkBaseInMultiProject() {
        for (ProjectGenerationContext projectGenerationContext : listProjectGenerationContexts) {
            if ( projectGenerationContext.getDatabase() == null)
            { return false; }
        }
        return true;
    }
    private void listenerAddBase(){
        databaseConfigurationForm.getAddDatabaseButton().addActionListener(e -> updateDataModelMulti());
    }
    @Override
    public void updateStep() {
        SwingUtilities.invokeLater(() -> {
            ignoreUpdates = true;
            boolean isMultiProject = !listProjectGenerationContexts.isEmpty();
            databaseConfigurationForm.refreshUI(isMultiProject);

            ignoreUpdates = false;
        });
    }

    @Override
    public JComponent getComponent() {
        return databaseConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        if (!checkBaseInMultiProject()) {
            Messages.showErrorDialog(
                    databaseConfigurationForm.getMainPanel(),
                    "One or more projects don't have a database",
                    "Error"
            );
            throw new IllegalArgumentException("Error: one or more projects don't have a database");
        }
        // Retrieve the selected database
        Database selectedDatabase = (Database) databaseConfigurationForm.getDmsOptions().getSelectedItem();

        if (selectedDatabase == null) {
            return;
        }
        try {
            // Update context and attempt connection
            updateContextAndEstablishConnection(selectedDatabase);

            // Update the UI with success feedback
            databaseConfigurationForm.getConnectionStatusLabel().setText("<html>Connection successful!</html>");
            databaseConfigurationForm.getConnectionStatusLabel().setForeground(JBColor.GREEN);
            databaseConfigurationForm.setConnectionSuccessful(true);

        } catch (Exception e) {
            // Update the UI with error feedback
            String formattedMessageHtml = formatErrorMessageHtml(e.getMessage());
            databaseConfigurationForm.getConnectionStatusLabel().setText("<html>Connection failed:<br>" + formattedMessageHtml + "</html>");
            databaseConfigurationForm.getConnectionStatusLabel().setForeground(JBColor.RED);
            databaseConfigurationForm.setConnectionSuccessful(false);

            Messages.showErrorDialog(
                    databaseConfigurationForm.getMainPanel(),
                    "Connection failed: " + e.getMessage(),
                    "Error"
            );


            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();

        }
    }
    private void updateDataModelMulti() {
        Database selectedDatabase = (Database) databaseConfigurationForm.getDmsOptions().getSelectedItem();
        if (selectedDatabase == null) {
            return;
        }
        try {
            if (multivalidate()) {
                // Update context and attempt connection
                ProjectGenerationContext newProjectGenerationContext = (ProjectGenerationContext)databaseConfigurationForm.getContextList().getSelectedItem() ;
                updateContextAndEstablishConnection(selectedDatabase , newProjectGenerationContext );
                // Update the UI with success feedback
                databaseConfigurationForm.getConnectionStatusLabel().setText("<html>Connection successful!</html>");
                databaseConfigurationForm.getConnectionStatusLabel().setForeground(JBColor.GREEN);
                databaseConfigurationForm.setConnectionSuccessful(true);
                Messages.showInfoMessage(
                        databaseConfigurationForm.getMainPanel(),
                        "Add database successful!",
                        "Success"
                );
            }
        } catch (Exception e) {
            // Update the UI with error feedback
            String formattedMessageHtml = formatErrorMessageHtml(e.getMessage());
            databaseConfigurationForm.getConnectionStatusLabel().setText("<html>Connection failed:<br>" + formattedMessageHtml + "</html>");
            databaseConfigurationForm.getConnectionStatusLabel().setForeground(JBColor.RED);
            databaseConfigurationForm.setConnectionSuccessful(false);
            Messages.showErrorDialog(
                    databaseConfigurationForm.getMainPanel(),
                    "Connection failed: " + e.getMessage(),
                    "Error"
            );
            System.err.println("Connection failed: " + e.getMessage());
            e.printStackTrace();

        }
    }

    /**
     * Updates the context with the selected database and credentials, then establishes a connection.
     *
     * @param selectedDatabase The selected database.
     * @throws Exception If an error occurs during the connection.
     */
    private void updateContextAndEstablishConnection(Database selectedDatabase) throws Exception {
        // Update database in the context
        generationContextManager.getContext().setDatabase(selectedDatabase);

        // Create credentials from the form inputs
        Credentials credentials = selectedDatabase.getCredentials();
        generationContextManager.getContext().setCredentials(credentials);

        // Close existing connection if any
        if (generationContextManager.getContext().getConnection() != null) {
            generationContextManager.getContext().getConnection().close();
        }
        /// TODO: Add an else block here to avoid the connection error at second startup
        // Establish a new connection and update the context
        generationContextManager.getContext().setConnection(selectedDatabase.getConnection(credentials));
    }
    private void updateContextAndEstablishConnection(Database selectedDatabase , ProjectGenerationContext projectGenerationContext) throws Exception {
        // Update database in the context
        projectGenerationContext.setDatabase(selectedDatabase);

        // Create credentials from the form inputs
        Credentials credentials = selectedDatabase.getCredentials();
        projectGenerationContext.setCredentials(credentials);

        // Close existing connection if any
        if (projectGenerationContext.getConnection() != null) {
            try {
                if (!projectGenerationContext.getConnection().isClosed()) {
                    projectGenerationContext.getConnection().close();
                    System.out.println("Info: Previous database connection successfully closed.");
                }
            } catch (Exception e) {
                System.out.println("Info: Previous database connection could not be closed cleanly (" + e.getMessage() + "). Proceeding with new connection.");
                throw new ConfigurationException("Info: Previous database connection could not be closed cleanly (" + e.getMessage() + "). Proceeding with new connection.");
            }
        }
        
        // Establish a new connection and update the context
        projectGenerationContext.setConnection(selectedDatabase.getConnection(credentials));
    }

    @Override
    public boolean validate() throws ConfigurationException {
        // Validate required fields
        validateRequiredFields();

        // Validate port number
        validatePort();

        // Validate database-specific fields
        validateDatabaseSpecificFields();

        // Vérifiez la connexion
        if (!databaseConfigurationForm.isConnectionSuccessful()) {
            throw new ConfigurationException("Cannot proceed: Database connection failed. Please test the connection and ensure it is successful.");
        }
        return true; // Si toutes les validations passent
    }
    public boolean multivalidate() throws ConfigurationException {
        if (ignoreUpdates) return true;
        validateRequiredFields();

        validatePort();

        validateDatabaseSpecificFields();

        if (!databaseConfigurationForm.isConnectionSuccessful()) {
            throw new ConfigurationException("Cannot proceed: Database connection failed. Please test the connection and ensure it is successful.");
        }
        return true;
    }

    private void validateRequiredFields() throws ConfigurationException {
        String host = databaseConfigurationForm.getHostField().getText().trim();
        String portStr = databaseConfigurationForm.getPortField().getText().trim();
        String databaseName = databaseConfigurationForm.getDatabaseField().getText().trim();
        String username = databaseConfigurationForm.getUsernameField().getText().trim();

        HashMap<String, String> options = new HashMap<>() {{
            put(host, "Host field cannot be empty.");
            put(portStr, "Port field cannot be empty.");
            put(databaseName, "Database name cannot be empty.");
            put(username, "Username cannot be empty");
        }};
        for (Map.Entry<String, String> e : options.entrySet()) {
            if (e.getKey().isEmpty()) {
                throw new ConfigurationException(e.getValue());
            }
        }
    }

    private void validatePort() throws ConfigurationException {
        String portStr = databaseConfigurationForm.getPortField().getText().trim();

        try {
            int port = Integer.parseInt(portStr);
            if (port <= 0 || port > 65535) {
                throw new ConfigurationException("Port must be between 1 and 65535.");
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Port must be a valid integer.");
        }
    }

    private void validateDatabaseSpecificFields() throws ConfigurationException {
        Database selectedDatabase = (Database) databaseConfigurationForm.getDmsOptions().getSelectedItem();
        if (selectedDatabase == null || !"Oracle".equals(selectedDatabase.getName())) {
            return;
        }
        String sid = databaseConfigurationForm.getSidField().getText().trim();
        String driverType = databaseConfigurationForm.getDriverNameField().getText().trim();

        if (sid.isEmpty()) {
            throw new ConfigurationException("SID field cannot be empty for Oracle databases.");
        }
        if (driverType.isEmpty()) {
            throw new ConfigurationException("Driver Type field cannot be empty for Oracle databases.");
        }
    }

    @Override
    public boolean isStepVisible() {
        return this.generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess();
    }
}
