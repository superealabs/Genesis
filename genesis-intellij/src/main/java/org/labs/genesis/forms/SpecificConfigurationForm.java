package org.labs.genesis.forms;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkMVC;

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
    private JLabel securityTypeLabel;
    private JComboBox<String> securityTypeOptions;
    private JLabel cacheProviderLabel;
    private JComboBox<String> cacheProviderOptions;
    private JLabel cacheableLabel;
    private JScrollPane allTablesAndViewsNamesPane;
    private JBList<String> selectedTableAndViewNamesList;

    private JCheckBox enableAuthCheckBox;
    @Setter
    private List<String> allTablesAndViewsNames = new ArrayList<>();

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
        securityTypeLabel.setVisible(true);
        securityTypeOptions.setVisible(true);
        cacheProviderLabel.setVisible(true);
        cacheProviderOptions.setVisible(true);
    }

    public void updateFormWithFramework(Framework framework) {
        hideAllDependentComponents();

        if (framework != null) {
            // Configurer loggingLevel
            configureLoggingLevel(framework);
            // Configurer type de sécurité
            configureSecurityType(framework);
            // Configure cache provider
            configureCacheProvider(framework);

            if (framework.getIsGateway()) {
                configureGatewayComponents();
            }
            if (frameworkUsesDatabase(framework)) {
                configureDatabaseComponents(framework);
            }
        }

        if (framework instanceof FrameworkMVC) {
            useAnEurekaServerCheckBox.setVisible(false);
            eurekaServerHostLabel.setVisible(false);
            eurekaServerHostField.setVisible(false);

            // Afficher le checkbox venv uniquement pour Django
            if (framework.getCoreFramework() != null && framework.getCoreFramework().equalsIgnoreCase("Django")) {

                if (enableAuthCheckBox != null) {
                    enableAuthCheckBox.setVisible(true);
                    enableAuthCheckBox.setSelected(true); // Par défaut, activer l'authentification
                }
            } else {
                if (enableAuthCheckBox != null) {
                    enableAuthCheckBox.setVisible(false);
                }
            }
        } else {
            // Masquer le checkbox venv pour les autres frameworks

            if (enableAuthCheckBox != null) {
                enableAuthCheckBox.setVisible(false);
            }
        }
    }

    public void updateFormWithTablesAndViews(List<String> selectedValues, List<String> selectedViewValues) {
        if (!selectedValues.isEmpty() || !selectedViewValues.isEmpty()) {
            // Configure selected tables and views
            configureSelectedTablesAndViews(selectedValues, selectedViewValues);
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

        // Masquer la liste des tables & views
        cacheableLabel.setVisible(false);
        selectedTableAndViewNamesList.setVisible(false);
        allTablesAndViewsNamesPane.setVisible(false);

        // Masquer le checkbox venv par défaut

        // Masquer le checkbox d'authentification par défaut
        if (enableAuthCheckBox != null) {
            enableAuthCheckBox.setVisible(false);
        }

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

    private void configureSecurityType(Framework framework) {
        securityTypeLabel.setVisible(true);
        securityTypeOptions.setVisible(true);

        securityTypeOptions.removeAllItems();
        framework.getConfigurations().stream()
                .filter(config -> "securityType".equals(config.getVariableName()))
                .flatMap(config -> config.getOptions().stream())
                .forEach(option -> securityTypeOptions.addItem(option));
    }

    private void configureCacheProvider(Framework framework) {
        cacheProviderLabel.setVisible(true);
        cacheProviderOptions.setVisible(true);

        cacheProviderOptions.removeAllItems();
        framework.getConfigurations().stream()
                .filter(config -> "cacheProvider".equals(config.getVariableName()))
                .flatMap(config -> config.getOptions().stream())
                .forEach(option -> cacheProviderOptions.addItem(option));

        // Add ActionListener to cacheProviderOptions
        cacheProviderOptions.addActionListener(e -> {
            String selectedOption = (String) cacheProviderOptions.getSelectedItem();
            boolean showCacheComponents = selectedOption != null && !selectedOption.equalsIgnoreCase("NONE");

            cacheableLabel.setVisible(showCacheComponents);
            selectedTableAndViewNamesList.setVisible(showCacheComponents);
            allTablesAndViewsNamesPane.setVisible(showCacheComponents);
        });
    }

    private void configureSelectedTablesAndViews(List<String> selectedValues, List<String> selectedViewValues) {
        this.allTablesAndViewsNames.addAll(selectedValues);
        this.allTablesAndViewsNames.addAll(selectedViewValues);
        selectedTableAndViewNamesList.setListData(this.allTablesAndViewsNames.toArray(new String[0]));
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
        String[] columnNames = { "Route ID", "URI", "Path", "Methods" };
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Autoriser l'édition de toutes les cellules
            }
        };

        // Ajouter une ligne initiale
        model.addRow(new Object[] { "", "", "", "" });
        routeConfigurationOption.setModel(model);

        // Personnaliser les couleurs de sélection
        Color selectionColor = new JBColor(new Color(173, 216, 230), new Color(0, 105, 148));
        routeConfigurationOption.setSelectionBackground(selectionColor);
        routeConfigurationOption.setSelectionForeground(JBColor.BLACK);

        scrollPaneRouteTable.setViewportView(routeConfigurationOption);
    }

    private void configureRouteButtons() {
        DefaultTableModel model = (DefaultTableModel) routeConfigurationOption.getModel();

        addRouteButton.addActionListener(e -> model.addRow(new Object[] { "", "", "", "" }));
        removeRouteButton.addActionListener(e -> {
            int selectedRow = routeConfigurationOption.getSelectedRow();
            if (selectedRow != -1) {
                model.removeRow(selectedRow);
            } else {
                Messages.showErrorDialog(
                        mainPanel,
                        "Please select a row to delete.",
                        "Error");
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