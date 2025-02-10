package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.forms.DatabaseConfigurationForm;

import javax.swing.*;

import static org.labs.genesis.Utils.formatErrorMessageHtml;

public class DatabaseConfigurationWizardStep extends ModuleWizardStep {
    private final DatabaseConfigurationForm databaseConfigurationForm;
    private final ProjectGenerationContext projectGenerationContext;

    public DatabaseConfigurationWizardStep(ProjectGenerationContext projectGenerationContext) {
        databaseConfigurationForm = new DatabaseConfigurationForm();
        this.projectGenerationContext = projectGenerationContext;
    }

    @Override
    public JComponent getComponent() {
        return databaseConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        // Retrieve the selected database
        Database selectedDatabase = (Database) databaseConfigurationForm.getDmsOptions().getSelectedItem();

        if (selectedDatabase != null) {
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
    }

    /**
     * Updates the context with the selected database and credentials, then establishes a connection.
     *
     * @param selectedDatabase The selected database.
     * @throws Exception If an error occurs during the connection.
     */
    private void updateContextAndEstablishConnection(Database selectedDatabase) throws Exception {
        // Update database in the context
        projectGenerationContext.setDatabase(selectedDatabase);

        // Create credentials from the form inputs
        Credentials credentials = selectedDatabase.getCredentials();
        projectGenerationContext.setCredentials(credentials);

        // Close existing connection if any
        if (projectGenerationContext.getConnection() != null) {
            projectGenerationContext.getConnection().close();
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


    private void validateRequiredFields() throws ConfigurationException {
        String host = databaseConfigurationForm.getHostField().getText().trim();
        String portStr = databaseConfigurationForm.getPortField().getText().trim();
        String databaseName = databaseConfigurationForm.getDatabaseField().getText().trim();
        String username = databaseConfigurationForm.getUsernameField().getText().trim();

        if (host.isEmpty()) {
            throw new ConfigurationException("Host field cannot be empty.");
        }
        if (portStr.isEmpty()) {
            throw new ConfigurationException("Port field cannot be empty.");
        }
        if (databaseName.isEmpty()) {
            throw new ConfigurationException("Database name cannot be empty.");
        }
        if (username.isEmpty()) {
            throw new ConfigurationException("Username field cannot be empty.");
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

        if (selectedDatabase != null && "Oracle".equalsIgnoreCase(selectedDatabase.getName())) {
            String sid = databaseConfigurationForm.getSidField().getText().trim();
            String driverType = databaseConfigurationForm.getDriverNameField().getText().trim();

            if (sid.isEmpty()) {
                throw new ConfigurationException("SID field cannot be empty for Oracle databases.");
            }
            if (driverType.isEmpty()) {
                throw new ConfigurationException("Driver Type field cannot be empty for Oracle databases.");
            }
        }
    }

}
