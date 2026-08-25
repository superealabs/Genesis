package org.labs.genesis.forms.components;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.data.VisualizationParameter;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class VisualizationConfigurationPanel extends JPanel {

    private static final int EDITOR_HEIGHT = 30;
    private static final int ROW_HEIGHT_MIN = 52;

    private final VisualizationPanel.VisualizationItem item;
    private final DashboardVisualComponent targetComponent;
    private final Runnable onBack;
    private final Runnable onDelete; // peut être null
    private final ScrollableContentPanel contentPanel;

    // Constructeur : onDelete peut être null
    public VisualizationConfigurationPanel(
            DashboardVisualComponent targetComponent,
            VisualizationPanel.VisualizationItem item,
            Runnable onBack,
            Runnable onDelete
    ) {
        this.item = item;
        this.targetComponent = targetComponent;
        this.onBack = onBack;
        this.onDelete = onDelete;

        setLayout(new BorderLayout());
        setOpaque(false);
        add(createHeader(), BorderLayout.NORTH);

        contentPanel = new ScrollableContentPanel();
        contentPanel.setOpaque(false);
        buildParameters();

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    // ============================================================
    // SCROLLABLE CONTENT PANEL
    //
    // Empêche le contenu de dépasser la largeur du viewport
    // (fix de l'overflow horizontal caché).
    // ============================================================

    private static class ScrollableContentPanel extends JPanel implements Scrollable {

        ScrollableContentPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            // Clé du fix : le panel ne dépasse jamais la largeur du viewport,
            // ses enfants doivent donc s'y adapter au lieu de déborder.
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private JComponent createHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(8, 10, 10, 10));

        // ============================================================
        // TOP : retour + titre
        // ============================================================

        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton backButton = new JButton(AllIcons.Actions.Back);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.setFocusable(false);
        backButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        backButton.setToolTipText("Back");
        backButton.addActionListener(e -> onBack.run());

        JLabel title = new JLabel(item.name);
        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 13f)
        );
        title.setForeground(DashboardTheme.TEXT);

        titleRow.add(backButton, BorderLayout.WEST);
        titleRow.add(title, BorderLayout.CENTER);

        header.add(titleRow);

        // ============================================================
        // DELETE : nouvelle ligne
        // ============================================================

        if (onDelete != null) {

            header.add(
                    Box.createVerticalStrut(8)
            );

            JButton deleteButton = createDeleteButton();

            header.add(deleteButton);
        }

        return header;
    }

    private JButton createDeleteButton() {

        JButton button = new JButton();

        button.setLayout(new BorderLayout(6, 0));

        // Icône native IntelliJ
        JLabel icon = new JLabel(AllIcons.Actions.Cancel);

        // Texte
        JLabel text = new JLabel("Delete");

        text.setForeground(
                DashboardTheme.TEXT_SECONDARY
        );

        text.setFont(
                text.getFont().deriveFont(Font.PLAIN, 11f)
        );

        button.add(icon, BorderLayout.WEST);
        button.add(text, BorderLayout.CENTER);

        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                DashboardTheme.BORDER_SUBTLE,
                                1
                        ),
                        new EmptyBorder(
                                5,
                                8,
                                5,
                                8
                        )
                )
        );

        button.setBackground(
                DashboardTheme.SURFACE_2
        );

        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setFocusable(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setToolTipText(
                "Delete this visualization from the canvas"
        );

        button.addActionListener(
                e -> onDelete.run()
        );

        // ============================================================
        // FIT CONTENT : on calcule la taille préférée réelle
        // (icône + texte + bordures/insets) au lieu de forcer
        // une largeur MAX_VALUE qui étire le bouton sur toute la ligne.
        // ============================================================

        Dimension preferred = button.getPreferredSize();
        Dimension fitSize = new Dimension(preferred.width, 32);

        button.setPreferredSize(fitSize);
        button.setMinimumSize(fitSize);
        button.setMaximumSize(fitSize); // <-- plus de Integer.MAX_VALUE ici

        button.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        return button;
    }

    // ============================================================
    // PARAMETERS
    // ============================================================

    private void buildParameters() {
        for (VisualizationParameter parameter : item.parameters) {
            JComponent editor = createEditor(parameter);

            // === NOUVEAU : liaison du champ titre ===
            if ("title".equals(parameter.getKey()) && targetComponent != null && editor instanceof JTextField) {
                JTextField titleField = (JTextField) editor;
                titleField.setText(targetComponent.getTitle());
                titleField.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) { updateTitle(); }
                    @Override
                    public void removeUpdate(DocumentEvent e) { updateTitle(); }
                    @Override
                    public void changedUpdate(DocumentEvent e) { updateTitle(); }

                    private void updateTitle() {
                        targetComponent.setTitle(titleField.getText());
                    }
                });
            }

            JPanel row = createParameterRow(parameter, editor);
            contentPanel.add(row);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        contentPanel.add(Box.createVerticalGlue());
    }

    private JPanel createParameterRow(
            VisualizationParameter parameter,
            JComponent editor
    ) {

        // BoxLayout vertical au lieu de BorderLayout : la hauteur
        // s'adapte au contenu de l'éditeur (utile pour les éditeurs
        // empilés comme COLUMN_OR_FORMULA / SORT), et surtout tout
        // reste contraint en largeur au lieu de forcer un minimum.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.setBorder(
                new EmptyBorder(
                        0,
                        10,
                        0,
                        10
                )
        );

        JLabel label =
                new JLabel(
                        parameter.getLabel()
                );

        label.setForeground(
                DashboardTheme.TEXT
        );

        label.setFont(
                label.getFont()
                        .deriveFont(
                                11f
                        )
        );

        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        editor.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(editor);

        // Largeur extensible (bornée par le viewport grâce à
        // ScrollableContentPanel), hauteur fixée au préféré.
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        panel.getPreferredSize().height
                )
        );

        return panel;
    }

    // ============================================================
    // EDITOR FACTORY
    // ============================================================

    private JComponent createEditor(
            VisualizationParameter parameter
    ) {

        return switch (
                parameter.getType()
                ) {

            case TEXT ->
                    createTextEditor();

            case NUMBER ->
                    createNumberEditor();

            case DB_COLUMN ->
                    createColumnEditor();

            case FORMULA ->
                    createFormulaEditor();

            case DB_COLUMN_OR_FORMULA ->
                    createColumnOrFormulaEditor();

            case SORT ->
                    createSortEditor();

            case COLUMNS ->
                    createColumnsEditor();
        };
    }

    // ============================================================
    // TEXT
    // ============================================================

    private JComponent createTextEditor() {

        JTextField field =
                new JTextField();

        styleField(field);

        return field;
    }

    // ============================================================
    // NUMBER
    // ============================================================

    private JComponent createNumberEditor() {

        JSpinner spinner =
                new JSpinner(
                        new SpinnerNumberModel(
                                10,
                                0,
                                Integer.MAX_VALUE,
                                1
                        )
                );

        setFixedHeight(
                spinner,
                EDITOR_HEIGHT
        );

        return spinner;
    }

    // ============================================================
    // DATABASE COLUMN
    // ============================================================

    private JComponent createColumnEditor() {

        JComboBox<String> combo =
                new JComboBox<>();

        combo.addItem(
                "Select column..."
        );

        /*
         * Plus tard :
         *
         * combo.addItem("customers.name");
         * combo.addItem("orders.amount");
         * combo.addItem("orders.created_at");
         */

        styleCombo(combo);

        return combo;
    }

    // ============================================================
    // FORMULA
    // ============================================================

    private JComponent createFormulaEditor() {

        JTextField field =
                new JTextField();

        field.putClientProperty(
                "JTextField.placeholderText",
                "Enter formula..."
        );

        styleField(field);

        return field;
    }

    // ============================================================
    // COLUMN OR FORMULA
    //
    // MODIFIE : empilé verticalement (mode au-dessus, champ en
    // dessous) au lieu de côte à côte, pour éviter que deux combos
    // dépassent la largeur de la sidebar.
    // ============================================================

    private JComponent createColumnOrFormulaEditor() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> mode =
                new JComboBox<>(
                        new String[]{
                                "Database Column",
                                "Formula"
                        }
                );

        styleCombo(mode);
        mode.setAlignmentX(Component.LEFT_ALIGNMENT);
        mode.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        JComboBox<String> column =
                new JComboBox<>();

        column.addItem(
                "Select column..."
        );

        styleCombo(column);
        column.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField formula =
                new JTextField();

        formula.putClientProperty(
                "JTextField.placeholderText",
                "Enter formula..."
        );

        styleField(formula);
        formula.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(mode);
        panel.add(Box.createVerticalStrut(6));
        panel.add(column);

        mode.addActionListener(
                e -> {

                    boolean isFormula =
                            mode.getSelectedIndex() == 1;

                    // L'éditeur actif (column ou formula) est
                    // toujours à l'index 2 du panel.
                    panel.remove(2);

                    JComponent toAdd = isFormula ? formula : column;
                    panel.add(toAdd, 2);

                    panel.revalidate();
                    panel.repaint();

                    // Le conteneur parent doit relayouter aussi
                    // (la hauteur préférée du panel change).
                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
        );

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height)
        );

        return panel;
    }

    // ============================================================
    // SORT
    //
    // MODIFIE : empilé verticalement (colonne au-dessus,
    // direction en dessous) au lieu de GridLayout côte à côte.
    // ============================================================

    private JComponent createSortEditor() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> column =
                new JComboBox<>();

        column.addItem(
                "Select column..."
        );

        JComboBox<String> direction =
                new JComboBox<>(
                        new String[]{
                                "Ascending",
                                "Descending"
                        }
                );

        styleCombo(column);
        styleCombo(direction);

        column.setAlignmentX(Component.LEFT_ALIGNMENT);
        direction.setAlignmentX(Component.LEFT_ALIGNMENT);

        column.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));
        direction.setMaximumSize(new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT));

        panel.add(column);
        panel.add(Box.createVerticalStrut(6));
        panel.add(direction);

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height)
        );

        return panel;
    }

    // ============================================================
    // COLUMNS
    // ============================================================

    private JComponent createColumnsEditor() {

        JPanel panel =
                new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
                new BoxLayout(
                        panel,
                        BoxLayout.Y_AXIS
                )
        );

        panel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JButton addButton =
                new JButton(
                        "+ Add column"
                );

        addButton.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        addButton.addActionListener(
                e -> {

                    JTextField field =
                            new JTextField();

                    field.putClientProperty(
                            "JTextField.placeholderText",
                            "Column name..."
                    );

                    styleField(field);

                    field.setAlignmentX(
                            Component.LEFT_ALIGNMENT
                    );

                    field.setMaximumSize(
                            new Dimension(Integer.MAX_VALUE, EDITOR_HEIGHT)
                    );

                    panel.add(
                            field,
                            panel.getComponentCount() - 1
                    );

                    panel.add(
                            Box.createVerticalStrut(5),
                            panel.getComponentCount() - 1
                    );

                    panel.revalidate();
                    panel.repaint();

                    contentPanel.revalidate();
                    contentPanel.repaint();
                }
        );

        panel.add(
                addButton
        );

        panel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height)
        );

        return panel;
    }

    // ============================================================
    // STYLE
    // ============================================================

    private void styleField(
            JTextField field
    ) {

        field.setForeground(
                DashboardTheme.TEXT
        );

        field.setCaretColor(
                DashboardTheme.TEXT
        );

        field.setBackground(
                DashboardTheme.SURFACE_2
        );

        setFixedHeight(
                field,
                EDITOR_HEIGHT
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                DashboardTheme.BORDER_SUBTLE,
                                1
                        ),
                        new EmptyBorder(
                                0,
                                8,
                                0,
                                8
                        )
                )
        );
    }

    private void styleCombo(
            JComboBox<?> combo
    ) {

        combo.setForeground(
                DashboardTheme.TEXT
        );

        combo.setBackground(
                DashboardTheme.SURFACE_2
        );

        setFixedHeight(
                combo,
                EDITOR_HEIGHT
        );
    }

    /**
     * Fixe uniquement la hauteur.
     *
     * La largeur est laissée au LayoutManager, mais on cappe
     * aussi maximumSize en largeur pour éviter qu'un composant
     * ne pousse le conteneur au-delà du viewport.
     */
    private void setFixedHeight(
            JComponent component,
            int height
    ) {

        Dimension preferred =
                component.getPreferredSize();

        component.setPreferredSize(
                new Dimension(
                        preferred.width,
                        height
                )
        );

        component.setMinimumSize(
                new Dimension(
                        0,
                        height
                )
        );

        component.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        height
                )
        );
    }
}