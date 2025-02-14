package org.labs.genesis.forms;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;

import static org.labs.genesis.Utils.formatErrorMessage;
import static org.labs.genesis.Utils.formatErrorMessageHtml;

@Getter
public class DatabaseConfigurationForm {
    private JPanel mainPanel;
    private JLabel dmsLabel;
    private JComboBox<Database> dmsOptions;
    private JLabel databaseLabel;
    private JTextField databaseField;
    private JLabel hostLabel;
    private JLabel usernameLabel;
    private JTextField hostField;
    private JTextField usernameField;
    private JTextField portField;
    private JLabel portLabel;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JTextField schemaField;
    private JCheckBox trustCertificateCheckBox;
    private JLabel schemaLabel;
    private JCheckBox useSSLCheckBox;
    private JCheckBox allowKeyRetrievalCheckBox;
    private JLabel driverNameLabel;
    private JTextField driverNameField;
    private JLabel URLLabel;
    private JTextField URLField;
    private JTextField sidField;
    private JLabel sidLabel;

    private LinkLabel<String> testConnectionButton;
    private JLabel connectionStatusLabel;

    @Setter
    private boolean connectionSuccessful = false;

    public DatabaseConfigurationForm() {
        populateDmsOptions();
        initializeDefaultValues();
        addListeners();

        if (dmsOptions.getItemCount() > 0) {
            dmsOptions.setSelectedIndex(0);
        }

        addTestConnectionButtonListener();
    }

    private void addTestConnectionButtonListener() {
        testConnectionButton.setListener((LinkLabel<String> source, String data) -> {
            // Retrieve the selected database
            Database selectedDatabase = (Database) dmsOptions.getSelectedItem();
            if (selectedDatabase != null) {
                // Build Credentials based on user input
                Credentials credentials = new Credentials(
                        databaseField.getText().trim(),
                        schemaField.getText().trim(),
                        usernameField.getText().trim(),
                        new String(passwordField.getPassword()),
                        hostField.getText().trim(),
                        portField.getText().trim(),
                        driverNameField.getText().trim(),
                        sidField.getText().trim(),
                        useSSLCheckBox.isSelected(),
                        allowKeyRetrievalCheckBox.isSelected(),
                        trustCertificateCheckBox.isSelected()
                );

                // Attempt to establish the connection
                try {
                    selectedDatabase.getConnection(credentials).close();

                    Messages.showInfoMessage(
                            mainPanel,
                            "Connection successful!",
                            "Success"
                    );
                    connectionStatusLabel.setText("<html>Connection successful!</html>");
                    connectionStatusLabel.setForeground(JBColor.GREEN);
                    connectionSuccessful = true;
                } catch (Exception ex) {
                    String formattedMessage = formatErrorMessage(ex.getMessage());
                    String formattedMessageHtml = formatErrorMessageHtml(ex.getMessage());

                    // Display the formatted error message in a popup
                    Messages.showErrorDialog(
                            mainPanel,
                            "Connection failed:\n" + formattedMessage,
                            "Error"
                    );

                    // Update the connection status label with the formatted HTML message
                    connectionStatusLabel.setText("<html>Connection failed:<br>" + formattedMessageHtml + "</html>");
                    connectionStatusLabel.setForeground(JBColor.RED);
                    connectionSuccessful = false;
                }
            }
        }, null);
    }


    private void populateDmsOptions() {
        // Add available databases to the combo box
        for (Database database : ProjectGenerator.databases.values()) {
            assert dmsOptions != null;
            dmsOptions.addItem(database);
        }
    }

    private void initializeDefaultValues() {
        Database selectedDatabase = (Database) getDmsOptions().getSelectedItem();

        // Set default values for text fields
        hostField.setText("localhost");
        assert selectedDatabase != null;
        portField.setText(selectedDatabase.getPort());
        usernameField.setText("root");
        passwordField.setText("");
        schemaField.setText("");
        driverNameField.setText(selectedDatabase.getDriverName());
        sidField.setText(selectedDatabase.getSid());

        // Default options for checkboxes
        trustCertificateCheckBox.setSelected(true);
        useSSLCheckBox.setSelected(true);
        allowKeyRetrievalCheckBox.setSelected(true);

        // Default URL
        if (dmsOptions.getItemCount() > 0) {
            updateJdbcUrl((Database) dmsOptions.getSelectedItem());
        }
    }

    private void addListeners() {
        // Listener for database selection change
        dmsOptions.addActionListener(e -> {
            Database selectedDatabase = (Database) dmsOptions.getSelectedItem();
            if (selectedDatabase != null) {
                updateFieldsForDatabase(selectedDatabase);
            }
        });

        // Listener for checkboxes affecting the JDBC URL
        ActionListener updateUrlActionListener = e -> updateJdbcUrl((Database) dmsOptions.getSelectedItem());
        trustCertificateCheckBox.addActionListener(updateUrlActionListener);
        useSSLCheckBox.addActionListener(updateUrlActionListener);
        allowKeyRetrievalCheckBox.addActionListener(updateUrlActionListener);

        // DocumentListener for text fields affecting the JDBC URL
        DocumentListener updateUrlDocumentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateJdbcUrl((Database) dmsOptions.getSelectedItem());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateJdbcUrl((Database) dmsOptions.getSelectedItem());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateJdbcUrl((Database) dmsOptions.getSelectedItem());
            }
        };

        // Add the DocumentListener to each text field
        hostField.getDocument().addDocumentListener(updateUrlDocumentListener);
        portField.getDocument().addDocumentListener(updateUrlDocumentListener);
        databaseField.getDocument().addDocumentListener(updateUrlDocumentListener);
        usernameField.getDocument().addDocumentListener(updateUrlDocumentListener);
        schemaField.getDocument().addDocumentListener(updateUrlDocumentListener);
        sidField.getDocument().addDocumentListener(updateUrlDocumentListener);
        driverNameField.getDocument().addDocumentListener(updateUrlDocumentListener);
    }


    private void updateFieldsForDatabase(Database database) {
        boolean isMysql = "Mysql".equalsIgnoreCase(database.getName());
        if (!isMysql) {
            trustCertificateCheckBox.setEnabled(false);
            useSSLCheckBox.setEnabled(false);
            allowKeyRetrievalCheckBox.setEnabled(false);
        } else {
            trustCertificateCheckBox.setEnabled(true);
            useSSLCheckBox.setEnabled(true);
            allowKeyRetrievalCheckBox.setEnabled(true);
        }

        // Adjust visible fields based on the selected database
        boolean isOracle = "Oracle".equalsIgnoreCase(database.getName());

        driverNameField.setEnabled(isOracle);
        sidField.setEnabled(isOracle);
        databaseField.setEnabled(!isOracle);

        // Specific default values
        portField.setText(database.getPort());
        if (isOracle) {
            sidField.setText(database.getSid());
            driverNameField.setText(database.getDriver());
        }

        // Update the JDBC URL
        updateJdbcUrl(database);
    }

    private void updateJdbcUrl(Database database) {
        if (database == null) return;

        // Build Credentials based on user input
        Credentials credentials = new Credentials(
                databaseField.getText().trim(),          // databaseName
                schemaField.getText().trim(),           // schemaName
                usernameField.getText().trim(),         // user
                new String(passwordField.getPassword()),// pwd
                hostField.getText().trim(),             // host
                portField.getText().trim(),             // port
                driverNameField.getText().trim(),       // driverType
                sidField.getText().trim(),              // SID
                useSSLCheckBox.isSelected(),            // useSSL
                allowKeyRetrievalCheckBox.isSelected(), // allowPublicKeyRetrieval
                trustCertificateCheckBox.isSelected()   // trustCertificate
        );

        database.setCredentials(credentials);

        // Generate the dynamic JDBC URL using the Credentials and the selected database
        String jdbcUrl = database.getJdbcUrl(credentials);

        // Update the URL field
        URLField.setText(jdbcUrl);
    }

}
