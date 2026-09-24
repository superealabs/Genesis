package org.labs.genesis.forms;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.form.FieldConfiguration;
import org.labs.genesis.config.form.FormConfiguration;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.Connection;
import java.util.*;
import java.util.List;

public class FormConfigurationForm {
    private Map<String, Map<String, FieldConfiguration>> tablesConf = new HashMap<>();

    private static final String CARD_EMPTY = "empty";
    private static final String CARD_CONFIG = "config";

    private static final String TYPE_ENUM = "enum";
    private static final String TYPE_GEOGRAPHIC = "geographic";
    private static final String TYPE_JSON = "json";
    private static final String TYPE_BINARY = "binary";
    private static final String TYPE_BOOLEAN = "boolean";
    private static final String TYPE_DATETIME = "datetime";
    private static final String TYPE_DATE = "date";
    private static final String TYPE_TIME = "time";
    private static final String TYPE_NUMBER = "number";
    private static final String TYPE_TEXT = "text";

    private final ProjectGenerationContext context;
    private final FormConfiguration formConfiguration;

    /** Root panel. Bound by the (now minimal) .form file, created here as a fallback. */
    @Getter
    private JPanel mainPanel;

    private JButton refreshButton;

    // Left pane
    private SearchTextField searchField;
    private JBLabel tableCountLabel;
    private JBList<TableMetadata> tableList;

    // Right pane
    private JPanel detailHeader;
    private JBLabel detailTitleLabel;
    private JBLabel detailSubtitleLabel;
    private JPanel detailCards;
    private CardLayout detailCardLayout;
    private JBScrollPane configScroll;
    private ScrollableColumn configurationPanel;

    // State
    private List<TableMetadata> tables = new ArrayList<>();
    private final Set<String> viewNames = new HashSet<>();

    /** Column configurations are kept per table so they survive switching tables. */
    private final Map<String, Map<String, ColumnConfiguration>> configurationsByTable = new HashMap<>();
    private Map<String, ColumnConfiguration> columnConfigurations = new HashMap<>();

    @Getter
    private TableMetadata selectedTable;
    private boolean updatingUI = false;
    private boolean updatingList = false;

    public FormConfigurationForm(ProjectGenerationContext context) {
        this.context = context;
        this.formConfiguration = ProjectGenerator.formConfiguration;

        setupUI();
        setupListeners();
        clearConfiguration();

        mainPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });
    }

    // ------------------------------------------------------------------
    // Data loading
    // ------------------------------------------------------------------

    public void loadMetadata() throws Exception {
        boolean noEntities = context.getEntityTables() == null || context.getEntityTables().isEmpty();
        boolean noViews = context.getViewTables() == null || context.getViewTables().isEmpty();

        if (!noEntities || !noViews) {
            return;
        }

        Database database = context.getDatabase();
        if (database == null) {
            return;
        }

        Connection connection = database.getConnection(context.getCredentials());
        if (connection == null) {
            return;
        }

        if (context.getEntityNames() == null || context.getEntityNames().isEmpty()
                || context.getViewNames() == null || context.getViewNames().isEmpty()) {

            context.setEntityNames(database.getAllTableNames(connection));
            context.setViewNames(database.getAllViewNames(connection));
        }

        context.setEntityTables(connection);
        context.setViewTables(database.getConnection(context.getCredentials()));
    }

    private void refreshData() {
        try {
            loadMetadata();
        } catch (Exception e) {
            System.out.println("Cannot initialize data: " + e.getMessage());
        }

        List<String> selectTables = new ArrayList<>();
        if (context.getEntityNames() != null) {
            selectTables.addAll(context.getEntityNames());
        }

        viewNames.clear();
        if (context.getViewNames() != null) {
            selectTables.addAll(context.getViewNames());
            viewNames.addAll(context.getViewNames());
        }

        this.tables = context.getAllTables().stream()
                .filter(table -> table.getTableName() != null)
                .filter(table -> selectTables.contains(table.getTableName()))
                .toList();

        // Forget configurations of tables that no longer exist
        Set<String> names = new HashSet<>(tables.size());
        for (TableMetadata table : tables) {
            names.add(table.getTableName());
        }
        configurationsByTable.keySet().retainAll(names);

        applyFilter();
    }

    /** Rebuilds the list model from {@link #tables} and the search text, keeping the selection if possible. */
    private void applyFilter() {
        String query = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase();

        String keep = selectedTable != null ? selectedTable.getTableName() : null;

        DefaultListModel<TableMetadata> model = new DefaultListModel<>();
        TableMetadata toSelect = null;

        for (TableMetadata table : tables) {
            if (!query.isEmpty() && !table.getTableName().toLowerCase().contains(query)) {
                continue;
            }
            model.addElement(table);
            if (table.getTableName().equals(keep)) {
                toSelect = table;
            }
        }

        updatingList = true;
        try {
            tableList.setModel(model);
            if (toSelect != null) {
                tableList.setSelectedValue(toSelect, true);
            } else {
                tableList.clearSelection();
            }
        } finally {
            updatingList = false;
        }

        tableCountLabel.setText(
                model.size() == tables.size()
                        ? String.valueOf(tables.size())
                        : model.size() + " of " + tables.size()
        );

        syncSelection();
    }

    private void syncSelection() {
        TableMetadata table = tableList.getSelectedValue();
        if (table == null) {
            clearConfiguration();
        } else if (table != selectedTable) {
            showTableConfiguration(table);
        }
    }

    // ------------------------------------------------------------------
    // UI construction
    // ------------------------------------------------------------------

    private void setupUI() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
        }

        mainPanel.setLayout(new BorderLayout(0, JBUI.scale(12)));
        mainPanel.setBorder(JBUI.Borders.empty(16, 20));
        mainPanel.setPreferredSize(JBUI.size(900, 650));
        mainPanel.setMinimumSize(JBUI.size(700, 500));

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createBody(), BorderLayout.CENTER);
    }

    private JComponent createHeader() {
        JBLabel title = new JBLabel("Form configuration");
        title.setFont(JBFont.label().biggerOn(5f).asBold());

        JBLabel subtitle = new JBLabel("Choose how each column is rendered in the generated forms");
        subtitle.setForeground(JBColor.GRAY);

        JPanel texts = new JPanel(new BorderLayout(0, JBUI.scale(2)));
        texts.add(title, BorderLayout.NORTH);
        texts.add(subtitle, BorderLayout.CENTER);

        refreshButton = new JButton("Refresh", AllIcons.Actions.Refresh);
        refreshButton.setToolTipText("Reload database tables and columns");

        JPanel actions = new JPanel(new GridBagLayout());
        actions.add(refreshButton);

        JPanel header = new JPanel(new BorderLayout());
        header.add(texts, BorderLayout.CENTER);
        header.add(actions, BorderLayout.EAST);
        header.setBorder(BorderFactory.createCompoundBorder(
                lineBorder(0, 0, 1, 0),
                JBUI.Borders.emptyBottom(12)
        ));

        return header;
    }

    private JComponent createBody() {
        JBSplitter splitter = new JBSplitter(false, 0.3f);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.setFirstComponent(createTablePane());
        splitter.setSecondComponent(createDetailPane());
        return splitter;
    }

    private JComponent createTablePane() {
        JPanel pane = new JPanel(new BorderLayout(0, JBUI.scale(8)));
        pane.setBorder(JBUI.Borders.emptyRight(12));
        pane.setMinimumSize(JBUI.size(200, 0));

        tableCountLabel = new JBLabel();
        tableCountLabel.setForeground(JBColor.GRAY);

        JPanel labelRow = new JPanel(new BorderLayout());
        labelRow.add(sectionLabel("Tables"), BorderLayout.WEST);
        labelRow.add(tableCountLabel, BorderLayout.EAST);

        searchField = new SearchTextField(false);

        JPanel top = new JPanel(new BorderLayout(0, JBUI.scale(6)));
        top.add(labelRow, BorderLayout.NORTH);
        top.add(searchField, BorderLayout.CENTER);

        tableList = new JBList<>(new DefaultListModel<>());
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.setCellRenderer(new TableRenderer());
        tableList.setFixedCellHeight(JBUI.scale(30));
        tableList.getEmptyText()
                .setText("No tables found")
                .appendLine("Check the database connection, then refresh");

        JBScrollPane scroll = new JBScrollPane(tableList);
        scroll.setBorder(lineBorder(1, 1, 1, 1));

        pane.add(top, BorderLayout.NORTH);
        pane.add(scroll, BorderLayout.CENTER);
        return pane;
    }

    private JComponent createDetailPane() {
        JPanel pane = new JPanel(new BorderLayout(0, JBUI.scale(8)));
        pane.setBorder(JBUI.Borders.emptyLeft(12));

        detailTitleLabel = new JBLabel();
        detailTitleLabel.setFont(JBFont.label().biggerOn(2f).asBold());

        detailSubtitleLabel = new JBLabel();
        detailSubtitleLabel.setForeground(JBColor.GRAY);

        detailHeader = new JPanel(new BorderLayout(0, JBUI.scale(2)));
        detailHeader.add(detailTitleLabel, BorderLayout.NORTH);
        detailHeader.add(detailSubtitleLabel, BorderLayout.CENTER);

        JBPanelWithEmptyText empty = new JBPanelWithEmptyText()
                .withEmptyText("Select a table to configure its columns");
        empty.setBorder(lineBorder(1, 1, 1, 1));

        configurationPanel = new ScrollableColumn();
        configurationPanel.setLayout(new BoxLayout(configurationPanel, BoxLayout.Y_AXIS));
        configurationPanel.setBorder(JBUI.Borders.empty(12));

        configScroll = new JBScrollPane(
                configurationPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        configScroll.setBorder(lineBorder(1, 1, 1, 1));
        configScroll.getVerticalScrollBar().setUnitIncrement(JBUI.scale(16));

        detailCardLayout = new CardLayout();
        detailCards = new JPanel(detailCardLayout);
        detailCards.add(empty, CARD_EMPTY);
        detailCards.add(configScroll, CARD_CONFIG);

        pane.add(detailHeader, BorderLayout.NORTH);
        pane.add(detailCards, BorderLayout.CENTER);
        return pane;
    }

    private void setupListeners() {
        tableList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || updatingList) {
                return;
            }
            syncSelection();
        });

        searchField.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                applyFilter();
            }
        });

        refreshButton.addActionListener(event -> refreshData());
    }

    // ------------------------------------------------------------------
    // Selection / detail pane
    // ------------------------------------------------------------------

    private void clearConfiguration() {
        selectedTable = null;
        columnConfigurations = new HashMap<>();

        configurationPanel.removeAll();
        configurationPanel.revalidate();
        configurationPanel.repaint();

        detailHeader.setVisible(false);
        detailCardLayout.show(detailCards, CARD_EMPTY);
    }

    private void showTableConfiguration(TableMetadata table) {
        selectedTable = table;
        columnConfigurations = configurationsByTable.computeIfAbsent(
                table.getTableName(),
                name -> new HashMap<>()
        );

        detailTitleLabel.setText(table.getTableName());
        detailHeader.setVisible(true);
        detailCardLayout.show(detailCards, CARD_CONFIG);

        rebuildConfigurationPanel();
        SwingUtilities.invokeLater(() -> configScroll.getVerticalScrollBar().setValue(0));
    }

    private void updateDetailSubtitle(int hiddenColumns) {
        if (selectedTable == null || selectedTable.getColumns() == null) {
            detailSubtitleLabel.setText("");
            return;
        }

        int total = selectedTable.getColumns().length;
        StringBuilder text = new StringBuilder()
                .append(total)
                .append(total == 1 ? " column" : " columns");

        if (hiddenColumns > 0) {
            text.append(" · ")
                    .append(hiddenColumns)
                    .append(" merged into other components");
        }

        if (viewNames.contains(selectedTable.getTableName())) {
            text.append(" · view");
        }

        detailSubtitleLabel.setText(text.toString());
    }

    private void rebuildConfigurationPanel() {
        updatingUI = true;
        int hidden = 0;

        try {
            configurationPanel.removeAll();

            if (selectedTable == null || selectedTable.getColumns() == null) {
                return;
            }

            for (ColumnMetadata column : selectedTable.getColumns()) {

                // Type générique non défini dans la configuration
                if (!isConfigurable(column)) {
                    hidden++;
                    continue;
                }

                ColumnConfiguration configuration = columnConfigurations.computeIfAbsent(
                        column.getName(),
                        name -> new ColumnConfiguration(column)
                );

                if (isColumnConsumed(column, configuration)) {
                    hidden++;
                    continue;
                }

                configurationPanel.add(createColumnCard(configuration));
                configurationPanel.add(Box.createVerticalStrut(JBUI.scale(10)));
            }

            configurationPanel.add(Box.createVerticalGlue());
        } finally {
            updatingUI = false;
            updateDetailSubtitle(hidden);
            configurationPanel.revalidate();
            configurationPanel.repaint();
        }
    }

    /** Rebuild after the current event has finished (we are usually inside a combo's listener). */
    private void rebuildLater() {
        SwingUtilities.invokeLater(this::rebuildConfigurationPanel);
    }

    // ------------------------------------------------------------------
    // Column card
    // ------------------------------------------------------------------

    private JPanel createColumnCard(ColumnConfiguration configuration) {
        RoundedCard card = new RoundedCard();
        card.setLayout(new GridBagLayout());
        card.setBorder(JBUI.Borders.empty(10, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints title = new GridBagConstraints();
        title.gridx = 0;
        title.gridy = 0;
        title.weightx = 1;
        title.anchor = GridBagConstraints.WEST;
        title.fill = GridBagConstraints.HORIZONTAL;
        card.add(createColumnTitle(configuration.column), title);

        GridBagConstraints selector = new GridBagConstraints();
        selector.gridx = 1;
        selector.gridy = 0;
        selector.anchor = GridBagConstraints.EAST;
        selector.insets = JBUI.insetsLeft(12);
        card.add(createComponentSelector(configuration), selector);

        List<FormConfiguration.Field> fields = getConfigurationFields(configuration);
        if (!fields.isEmpty()) {
            GridBagConstraints options = new GridBagConstraints();
            options.gridx = 0;
            options.gridy = 1;
            options.gridwidth = 2;
            options.weightx = 1;
            options.fill = GridBagConstraints.HORIZONTAL;
            options.insets = JBUI.insetsTop(10);
            card.add(createOptionsPanel(configuration, fields), options);
        }

        return card;
    }

    private boolean isConfigurable(ColumnMetadata column) {
        if (column == null) {
            return false;
        }

        String type = getConfigurationType(column);

        return type != null
                && formConfiguration != null
                && formConfiguration.getTypes() != null
                && formConfiguration.getTypes().containsKey(type);
    }

    private JComponent createColumnTitle(ColumnMetadata column) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JBLabel name = new JBLabel(column.getName());
        name.setFont(JBFont.label().asBold());

        JBLabel type = new JBLabel(describeType(column));
        type.setForeground(JBColor.GRAY);
        type.setFont(JBFont.small());

        panel.add(name);
        panel.add(Box.createHorizontalStrut(JBUI.scale(8)));
        panel.add(type);
        panel.add(Box.createHorizontalGlue());
        return panel;
    }

    private String describeType(ColumnMetadata column) {
        String type = column.getDatabaseColumnType() != null
                ? column.getDatabaseColumnType()
                : getConfigurationType(column);

        if (column.isForeign()) {
            type += " · foreign key";
        }
        return type;
    }

    private JComponent createComponentSelector(ColumnConfiguration configuration) {
        List<ComponentOption> options = getComponentOptions(configuration);

        if (options.isEmpty()) {
            JBLabel none = new JBLabel("No component available");
            none.setForeground(JBColor.GRAY);
            return none;
        }

        ComboBox<ComponentOption> combo = new ComboBox<>();

        // Première entrée = aucun choix utilisateur
        combo.addItem(null);

        for (ComponentOption option : options) {
            combo.addItem(option);
        }

        combo.setRenderer(new ComponentComboRenderer());

        // Si un component a déjà été choisi, on l'affiche.
        if (configuration.component != null) {
            for (int i = 1; i < combo.getItemCount(); i++) {
                ComponentOption option = combo.getItemAt(i);

                if (option.component.equals(configuration.component)
                        && Objects.equals(option.columnField, configuration.columnField)) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            combo.setSelectedIndex(0);
        }

        Dimension size = combo.getPreferredSize();
        combo.setPreferredSize(new Dimension(
                Math.max(size.width, JBUI.scale(200)),
                size.height
        ));

        combo.addActionListener(event -> {
            if (updatingUI) {
                return;
            }

            ComponentOption option =
                    (ComponentOption) combo.getSelectedItem();

            if (option == null) {
                configuration.component = null;
                configuration.columnField = null;
            } else {
                configuration.component = option.component;
                configuration.columnField = option.columnField;
                updateTableConfiguration(selectedTable.getTableName(),
                        configuration.column.getReferencedColumn(), option.component);
            }

            configuration.assignedColumns.clear();
            configuration.values.clear();

            rebuildLater();
        });

        return combo;
    }

    private JPanel createOptionsPanel(
            ColumnConfiguration configuration,
            List<FormConfiguration.Field> fields
    ) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                lineBorder(1, 0, 0, 0),
                JBUI.Borders.emptyTop(8)
        ));

        int row = 0;
        for (FormConfiguration.Field field : fields) {
            JComponent editor = createFieldEditor(field, configuration);
            boolean tall = editor instanceof JPanel;

            JBLabel label = new JBLabel(humanize(field.getName()));
            label.setForeground(JBColor.GRAY);

            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = row;
            labelConstraints.anchor = tall
                    ? GridBagConstraints.NORTHWEST
                    : GridBagConstraints.WEST;
            labelConstraints.insets = tall
                    ? JBUI.insets(6, 0, 3, 16)
                    : JBUI.insets(3, 0, 3, 16);

            GridBagConstraints editorConstraints = new GridBagConstraints();
            editorConstraints.gridx = 1;
            editorConstraints.gridy = row;
            editorConstraints.weightx = 1;
            editorConstraints.anchor = GridBagConstraints.WEST;
            editorConstraints.fill = GridBagConstraints.HORIZONTAL;
            editorConstraints.insets = JBUI.insets(3, 0);

            panel.add(label, labelConstraints);
            panel.add(editor, editorConstraints);
            row++;
        }

        return panel;
    }

    // ------------------------------------------------------------------
    // Field editors
    // ------------------------------------------------------------------

    private JComponent createFieldEditor(
            FormConfiguration.Field field,
            ColumnConfiguration configuration
    ) {
        String type = field.getType();

        return switch (type == null ? "" : type) {
            case "column" -> createColumnSelector(field, configuration);
            case "select" -> createSelect(field, configuration);
            case "multi-select" -> createMultiSelect(field, configuration);
            default -> createTextEditor(field, configuration, "number".equals(type));
        };
    }

    private JComponent createColumnSelector(
            FormConfiguration.Field field,
            ColumnConfiguration configuration
    ) {
        ComboBox<ColumnMetadata> combo = new ComboBox<>();
        combo.addItem(null);

        ColumnMetadata selected = configuration.assignedColumns.get(field.getName());

        for (ColumnMetadata column : selectedTable.getColumns()) {
            if (!isCompatibleColumn(column, field, configuration)) {
                continue;
            }
            if (isColumnConsumed(column, configuration)) {
                continue;
            }
            combo.addItem(column);
        }

        combo.setRenderer(new ColumnComboRenderer());
        combo.setSelectedItem(selected);

        combo.addActionListener(event -> {
            if (updatingUI) {
                return;
            }

            ColumnMetadata value =
                    (ColumnMetadata) combo.getSelectedItem();

            if (value == null) {
                configuration.assignedColumns.remove(field.getName());
            } else {
                configuration.assignedColumns.put(field.getName(), value);
            }

            updateFieldConfiguration(selectedTable.getTableName(),
                    configuration.columnField, field.getName(), field.getType(),
                    value != null ? selectedTable.getTableName() +
                            "." + value.getReferencedColumn() : null);

            rebuildLater();
        });

        return combo;
    }

    private JComponent createSelect(
            FormConfiguration.Field field,
            ColumnConfiguration configuration
    ) {
        ComboBox<String> combo = new ComboBox<>();

        if (field.getOptions() != null) {
            for (String option : field.getOptions()) {
                combo.addItem(option);
            }
        }

        String saved = configuration.values.get(field.getName());
        if (saved != null) {
            combo.setSelectedItem(saved);
        } else if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
            configuration.values.put(field.getName(), combo.getItemAt(0));
        }

        combo.addActionListener(event -> {
            if (updatingUI || combo.getSelectedItem() == null) {
                return;
            }
            configuration.values.put(field.getName(), (String) combo.getSelectedItem());
            updateFieldConfiguration(selectedTable.getTableName(), configuration.column.getReferencedColumn(), field.getName(), field.getType(), (String) combo.getSelectedItem());
        });

        return combo;
    }

    private JComponent createMultiSelect(
            FormConfiguration.Field field,
            ColumnConfiguration configuration
    ) {
        JPanel panel = new JPanel(new GridLayout(0, 2, JBUI.scale(12), JBUI.scale(2)));
        panel.setOpaque(false);

        Set<String> selected = splitValues(configuration.values.get(field.getName()));

        if (field.getOptions() != null) {
            for (String option : field.getOptions()) {
                JBCheckBox checkBox = new JBCheckBox(option, selected.contains(option));
                checkBox.setOpaque(false);

                checkBox.addActionListener(event -> {
                    if (updatingUI) {
                        return;
                    }
                    List<String> checked = new ArrayList<>();
                    for (Component child : panel.getComponents()) {
                        if (child instanceof JBCheckBox box && box.isSelected()) {
                            checked.add(box.getText());
                        }
                    }
                    configuration.values.put(field.getName(), String.join(",", checked));
                    updateFieldConfiguration(selectedTable.getTableName(),
                            configuration.column.getReferencedColumn(), field.getName(), field.getType(), String.join(",", checked));
                });

                panel.add(checkBox);
            }
        }

        return panel;
    }

    private JComponent createTextEditor(
            FormConfiguration.Field field,
            ColumnConfiguration configuration,
            boolean numeric
    ) {
        JBTextField textField = new JBTextField(
                configuration.values.getOrDefault(field.getName(), "")
        );

        if (numeric) {
            textField.getEmptyText().setText("Number");
        }

        textField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                if (!updatingUI) {
                    configuration.values.put(field.getName(), textField.getText());
                    updateFieldConfiguration(selectedTable.getTableName(), configuration.column.getReferencedColumn(), field.getName(), field.getType(), textField.getText());
                }
            }
        });

        return textField;
    }

    // ------------------------------------------------------------------
    // Configuration logic
    // ------------------------------------------------------------------

    private boolean isColumnConsumed(ColumnMetadata column, ColumnConfiguration current) {
        for (ColumnConfiguration configuration : columnConfigurations.values()) {
            if (configuration == current) {
                continue;
            }
            for (ColumnMetadata assigned : configuration.assignedColumns.values()) {
                if (assigned != null && assigned.getName().equals(column.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCompatibleColumn(
            ColumnMetadata column,
            FormConfiguration.Field field,
            ColumnConfiguration configuration
    ) {
        if (column == null) {
            return false;
        }
        if (column.getName().equals(configuration.column.getName())) {
            return false;
        }
        if ("map".equals(configuration.component)
                && ("latitude".equals(field.getName()) || "longitude".equals(field.getName()))) {
            return column.isNumeric();
        }
        return true;
    }

    private List<ComponentOption> getComponentOptions(ColumnConfiguration configuration) {
        String type = getConfigurationType(configuration.column);
        FormConfiguration.Type typeConfiguration = formConfiguration.getTypes().get(type);

        if (typeConfiguration == null || typeConfiguration.getComponents() == null) {
            return List.of();
        }

        List<ComponentOption> result = new ArrayList<>();
        for (String component : typeConfiguration.getComponents()) {
            List<FormConfiguration.Field> fields = getColumnFields(component);
            if (fields.isEmpty()) {
                result.add(new ComponentOption(component, null));
                continue;
            }
            for (FormConfiguration.Field field : fields) {
                result.add(new ComponentOption(component, field.getName()));
            }
        }
        return result;
    }

    private List<FormConfiguration.Field> getColumnFields(String component) {
        FormConfiguration.Configuration configuration = formConfiguration.getComponents().get(component);
        if (configuration == null
                || configuration.getConfiguration() == null
                || configuration.getConfiguration().getFields() == null) {
            return List.of();
        }
        return configuration.getConfiguration().getFields().stream()
                .filter(field -> "column".equals(field.getType()))
                .toList();
    }

    private List<FormConfiguration.Field> getConfigurationFields(ColumnConfiguration configuration) {
        if (configuration == null || configuration.component == null) {
            return List.of();
        }

        FormConfiguration.Configuration componentConfiguration =
                formConfiguration.getComponents().get(configuration.component);

        if (componentConfiguration == null
                || componentConfiguration.getConfiguration() == null
                || componentConfiguration.getConfiguration().getFields() == null) {
            return List.of();
        }

        return componentConfiguration.getConfiguration().getFields().stream()
                .filter(field -> !"column".equals(field.getType())
                        || !Objects.equals(field.getName(), configuration.columnField))
                .toList();
    }

    private String getConfigurationType(ColumnMetadata column) {
        if (column.isForeign()) {
            return TYPE_ENUM;
        }

        String databaseType = column.getDatabaseColumnType();
        if (databaseType != null) {
            String type = databaseType.toLowerCase();

            if (type.contains("geography") || type.contains("geometry")) {
                return TYPE_GEOGRAPHIC;
            }
            if (type.contains("json")) {
                return TYPE_JSON;
            }
            if (type.contains("bytea") || type.contains("binary") || type.contains("blob")) {
                return TYPE_BINARY;
            }
            if (type.contains("boolean")) {
                return TYPE_BOOLEAN;
            }
        }

        if (column.isDateTime() || column.isDateTimeTz()) {
            return TYPE_DATETIME;
        }
        if (column.isDate()) {
            return TYPE_DATE;
        }
        if (column.isTime() || column.isTimeTz()) {
            return TYPE_TIME;
        }
        if (column.isNumeric()) {
            return TYPE_NUMBER;
        }
        if(column.isText()) {
            return TYPE_TEXT;
        }

        return null;
    }

    // ------------------------------------------------------------------
    // Public accessors
    // ------------------------------------------------------------------

    public List<ColumnMetadata> getSelectedColumns() {
        if (selectedTable == null || selectedTable.getColumns() == null) {
            return List.of();
        }
        return Arrays.asList(selectedTable.getColumns());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Border lineBorder(int top, int left, int bottom, int right) {
        return JBUI.Borders.customLine(JBColor.border(), top, left, bottom, right);
    }

    private static JBLabel sectionLabel(String text) {
        JBLabel label = new JBLabel(text);
        label.setFont(JBFont.label().asBold());
        return label;
    }

    private static String humanize(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }
        String text = name.replace('_', ' ').replace('-', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static Set<String> splitValues(String value) {
        if (value == null || value.isEmpty()) {
            return Set.of();
        }

        Set<String> result = new HashSet<>();
        for (String token : value.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    // ------------------------------------------------------------------
    // Nested types
    // ------------------------------------------------------------------

    private static class ColumnConfiguration {

        private final ColumnMetadata column;
        private String component;
        private String columnField;

        private final Map<String, ColumnMetadata> assignedColumns = new HashMap<>();
        /** Values of the non-column fields (text, number, select, multi-select). */
        private final Map<String, String> values = new HashMap<>();

        private ColumnConfiguration(ColumnMetadata column) {
            this.column = column;
        }
    }

    private static class ComponentOption {

        private final String component;
        private final String columnField;

        private ComponentOption(String component, String columnField) {
            this.component = component;
            this.columnField = columnField;
        }

        @Override
        public String toString() {
            return columnField == null ? component : component + " (" + columnField + ")";
        }
    }

    /** Vertical stack that follows the viewport width so cards never scroll horizontally. */
    private static class ScrollableColumn extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return JBUI.scale(16);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(visibleRect.height - JBUI.scale(16), JBUI.scale(16));
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /** Rounded, theme-aware card that hosts the configuration of one column. */
    private static class RoundedCard extends JPanel {

        private RoundedCard() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                int arc = JBUI.scale(10);
                int width = getWidth() - 1;
                int height = getHeight() - 1;

                g2.setColor(UIUtil.getListBackground());
                g2.fillRoundRect(0, 0, width, height, arc, arc);

                g2.setColor(JBColor.border());
                g2.drawRoundRect(0, 0, width, height, arc, arc);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public Dimension getMaximumSize() {
            // Stretch horizontally, never vertically (BoxLayout would otherwise inflate the card)
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    private class TableRenderer extends ColoredListCellRenderer<TableMetadata> {

        @Override
        protected void customizeCellRenderer(
                @NotNull JList<? extends TableMetadata> list,
                TableMetadata value,
                int index,
                boolean selected,
                boolean hasFocus
        ) {
            setIpad(JBUI.insets(4, 8));

            if (value == null) {
                return;
            }

            String name = value.getTableName() != null ? value.getTableName() : "Unnamed table";
            setIcon(AllIcons.Nodes.DataTables);
            append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES);

            if (viewNames.contains(name)) {
                append("  view", SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
            }
        }
    }

    private static class ComponentComboRenderer
            extends ColoredListCellRenderer<ComponentOption> {

        @Override
        protected void customizeCellRenderer(
                @NotNull JList<? extends ComponentOption> list,
                ComponentOption value,
                int index,
                boolean selected,
                boolean hasFocus
        ) {
            if (value == null) {
                append(
                        "Default component",
                        SimpleTextAttributes.GRAYED_ATTRIBUTES
                );
                return;
            }

            append(
                    value.toString(),
                    SimpleTextAttributes.REGULAR_ATTRIBUTES
            );
        }
    }

    private static class ColumnComboRenderer extends ColoredListCellRenderer<ColumnMetadata> {

        @Override
        protected void customizeCellRenderer(
                @NotNull JList<? extends ColumnMetadata> list,
                ColumnMetadata value,
                int index,
                boolean selected,
                boolean hasFocus
        ) {
            if (value == null) {
                append("None", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                return;
            }

            append(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            if (value.getDatabaseColumnType() != null) {
                append(
                        "  " + value.getDatabaseColumnType(),
                        SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES
                );
            }
        }
    }

    private void updateTableConfiguration(String tableName, String columnName, String component) {
        if (tableName == null || tableName.isBlank()
                || columnName == null || columnName.isBlank()) {
            return;
        }

        Map<String, FieldConfiguration> tableConf =
                tablesConf.computeIfAbsent(tableName, k -> new HashMap<>());

        if (component == null || component.isBlank()) {
            tableConf.remove(columnName);
            if (tableConf.isEmpty()) {
                tablesConf.remove(tableName);
            }
            return;
        }

        FieldConfiguration fieldConf =
                tableConf.computeIfAbsent(columnName, k -> new FieldConfiguration());

        fieldConf.setComponent(component);
    }

    private void updateFieldConfiguration(String tableName, String columnName, String configuration, String type, String value) {
        if(tableName == null || tableName.isBlank() || columnName == null
                || columnName.isBlank()) return;

        Map<String, FieldConfiguration> tableConf = tablesConf.get(tableName);
        if(tableConf != null) {
            FieldConfiguration fieldConf = tableConf.get(columnName);
            if (fieldConf != null) {
                if (configuration != null && value != null && !value.isBlank()) {
                    fieldConf.addConfig(configuration, value, type);
                } else if (configuration != null) {
                    fieldConf.removeConf(configuration);
                }
            }
        }
    }

    public Map<String, Map<String, FieldConfiguration>>getTablesConfiguration() {
        return tablesConf;
    }
}