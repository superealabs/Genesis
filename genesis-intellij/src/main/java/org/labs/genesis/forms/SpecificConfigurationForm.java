package org.labs.genesis.forms;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import org.labs.genesis.config.langage.Framework;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.labs.genesis.wizards.SpecificConfigurationWizardStep.frameworkHasConfiguration;

@Getter
public class SpecificConfigurationForm {
    private JPanel mainPanel;
    private JTextField projectPortField;
    private JLabel projectPortLabel;
    private JLabel projectDescriptionLabel;
    private JTextField projectDescriptionField;
    private JLabel loggingLevelLabel;
    private JComboBox<String> loggingLevelOptions;
    private JComboBox<String> ddlAutoOptions;
    private JLabel hibernateDDLAutoLabel;
    private JCheckBox useAnEurekaServerCheckBox;
    private JLabel routeConfigurationLabel;
    private JTextField eurekaServerHostField;
    private JLabel eurekaServerHostLabel;
    private JScrollPane scrollPaneRouteTable;
    private JTable routeConfigurationOption;
    private JButton addRouteButton;
    private JButton removeRouteButton;
    private JLabel defaultUsernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel roleLabel;
    private JTextField roleField;

    public void initializeForm() {
        // Masquer tous les composants dépendants au début
        hideAllDependentComponents();

        // Configurer les événements de la case à cocher Eureka
        configureEurekaCheckbox();

        // Configurer la table de routes et les boutons
        initializeRouteConfigurationTable();
        configureRouteButtons();

        // Afficher les composants toujours visibles
        loggingLevelLabel.setVisible(true);
        loggingLevelOptions.setVisible(true);
        useAnEurekaServerCheckBox.setVisible(true);
    }

    public void updateFormWithFramework(Framework framework) {
        hideAllDependentComponents();

        if (framework != null) {
            // Configurer loggingLevel
            configureLoggingLevel(framework);

            if (framework.getIsGateway()) {
                configureGatewayComponents();
            }
            if (frameworkUsesDatabase(framework)) {
                configureDatabaseComponents(framework);
            }
        }
    }

    private void hideAllDependentComponents() {
        // Masquer les composants de Gateway
        scrollPaneRouteTable.setVisible(false);
        routeConfigurationLabel.setVisible(false);
        routeConfigurationOption.setVisible(false);
        addRouteButton.setVisible(false);
        removeRouteButton.setVisible(false);
        defaultUsernameLabel.setVisible(false);
        usernameField.setVisible(false);
        passwordLabel.setVisible(false);
        passwordField.setVisible(false);
        roleLabel.setVisible(false);
        roleField.setVisible(false);

        // Masquer les composants de base de données
        hibernateDDLAutoLabel.setVisible(false);
        ddlAutoOptions.setVisible(false);

        // Désactiver Eureka par défaut
        useAnEurekaServerCheckBox.setSelected(false);
        eurekaServerHostField.setEnabled(false);
    }

    private void configureEurekaCheckbox() {
        useAnEurekaServerCheckBox.addActionListener(e -> {
            boolean selected = useAnEurekaServerCheckBox.isSelected();
            eurekaServerHostField.setEnabled(selected);
        });
    }

    private void configureLoggingLevel(Framework framework) {
        loggingLevelLabel.setVisible(true);
        loggingLevelOptions.setVisible(true);

        loggingLevelOptions.removeAllItems();
        framework.getConfigurations().stream()
                .filter(config -> "loggingLevel".equals(config.getVariableName()))
                .flatMap(config -> config.getOptions().stream())
                .forEach(option -> loggingLevelOptions.addItem(option));
    }

    private void configureGatewayComponents() {
        scrollPaneRouteTable.setVisible(true);
        routeConfigurationLabel.setVisible(true);
        routeConfigurationOption.setVisible(true);
        addRouteButton.setVisible(true);
        removeRouteButton.setVisible(true);

        defaultUsernameLabel.setVisible(true);
        usernameField.setVisible(true);
        passwordLabel.setVisible(true);
        passwordField.setVisible(true);
        roleLabel.setVisible(true);
        roleField.setVisible(true);
    }

    private void configureDatabaseComponents(Framework framework) {
        if (frameworkHasConfiguration(framework, "hibernateDdlAuto")) {
            hibernateDDLAutoLabel.setVisible(true);
            ddlAutoOptions.setVisible(true);

            ddlAutoOptions.removeAllItems();
            framework.getConfigurations().stream()
                    .filter(config -> "hibernateDdlAuto".equals(config.getVariableName()))
                    .flatMap(config -> config.getOptions().stream())
                    .forEach(option -> ddlAutoOptions.addItem(option));
        }
    }

    private boolean frameworkUsesDatabase(Framework framework) {
        return frameworkHasConfiguration(framework, "hibernateDdlAuto");
    }

    private void initializeRouteConfigurationTable() {
        String[] columnNames = {"Route ID", "URI", "Path", "Methods"};
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Autoriser l'édition de toutes les cellules
            }
        };

        // Ajouter une ligne initiale
        model.addRow(new Object[]{"", "", "", ""});
        routeConfigurationOption.setModel(model);

        // Personnaliser les couleurs de sélection
        Color selectionColor = new JBColor(new Color(173, 216, 230), new Color(0, 105, 148));
        routeConfigurationOption.setSelectionBackground(selectionColor);
        routeConfigurationOption.setSelectionForeground(JBColor.BLACK);

        scrollPaneRouteTable.setViewportView(routeConfigurationOption);
    }

    private void configureRouteButtons() {
        DefaultTableModel model = (DefaultTableModel) routeConfigurationOption.getModel();

        addRouteButton.addActionListener(e -> model.addRow(new Object[]{"", "", "", ""}));
        removeRouteButton.addActionListener(e -> {
            int selectedRow = routeConfigurationOption.getSelectedRow();
            if (selectedRow != -1) {
                model.removeRow(selectedRow);
            } else {
                Messages.showErrorDialog(
                        mainPanel,
                        "Please select a row to delete.",
                        "Error"
                );
            }
        });
    }

    public List<Map<String, String>> getRouteConfigurationData() {
        DefaultTableModel model = (DefaultTableModel) routeConfigurationOption.getModel();
        List<Map<String, String>> routes = new ArrayList<>();

        for (int i = 0; i < model.getRowCount(); i++) {
            Map<String, String> route = new HashMap<>();
            route.put("id", (String) model.getValueAt(i, 0));
            route.put("uri", (String) model.getValueAt(i, 1));
            route.put("path", (String) model.getValueAt(i, 2));
            route.put("method", (String) model.getValueAt(i, 3));
            routes.add(route);
        }
        return routes;
    }
}