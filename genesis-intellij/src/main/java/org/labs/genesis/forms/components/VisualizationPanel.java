package org.labs.genesis.forms.components;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class VisualizationPanel extends JPanel {

    private final JPanel chartsGrid;
    private final JScrollPane scrollPane;

    private JTextField searchField;

    private static final int CARD_SIZE = 64;
    private static final int COLUMNS = 3;
    private static final int CARD_GAP = 8;
    private static final int CARD_RADIUS = 16;
    private VisualizationSelectionListener selectionListener;


    /*
     * Toutes les visualisations disponibles.
     *
     * Pour ajouter une nouvelle visualisation :
     *
     * 1. Ajouter l'image dans :
     *    src/main/resources/data_genesis/img/
     *
     * 2. Ajouter simplement un VisualizationItem ici.
     *
     * Aucun renderer Java n'est nécessaire.
     */
    private final List<VisualizationItem> visualizations = List.of(

            new VisualizationItem(
                    "Bar Chart Vertical",
                    "Vertical bar chart for comparing categories",
                    "/data_genesis/img/barchart-vertical.png"
            ),

            new VisualizationItem(
                    "Bar Chart Horizontal",
                    "Horizontal bar chart for ranking data",
                    "/data_genesis/img/barchart-horizontal.png"
            ),

            new VisualizationItem(
                    "Pie Chart",
                    "Distribution of data in percentages",
                    "/data_genesis/img/piechart.png"
            ),

            new VisualizationItem(
                    "Donut Chart",
                    "Pie chart with a hole in the center",
                    "/data_genesis/img/donutchart.png"
            ),

            new VisualizationItem(
                    "Line Chart",
                    "Trends over time or continuous data",
                    "/data_genesis/img/linechart.png"
            ),

            new VisualizationItem(
                    "Gauge",
                    "Single value with min/max range",
                    "/data_genesis/img/jauge.png"
            ),

            new VisualizationItem(
                    "KPI Card",
                    "Key performance indicator with value and trend",
                    "/data_genesis/img/kpi-card.png"
            ),

            new VisualizationItem(
                    "Table",
                    "Structured data in rows and columns",
                    "/data_genesis/img/table.png"
            ),

            new VisualizationItem(
                    "Map",
                    "Geographical data visualization",
                    "/data_genesis/img/map.png"
            )
    );

    public VisualizationPanel() {

        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel searchBar =
                createSearchBar(
                        "Search visualizations..."
                );

        add(
                searchBar,
                BorderLayout.NORTH
        );

        chartsGrid =
                new JPanel(
                        new GridBagLayout()
                );

        chartsGrid.setOpaque(false);

        chartsGrid.setBorder(
                BorderFactory.createEmptyBorder(
                        6,
                        6,
                        6,
                        6
                )
        );

        scrollPane =
                new JScrollPane(chartsGrid);

        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        scrollPane.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scrollPane.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);

        add(
                scrollPane,
                BorderLayout.CENTER
        );

        /*
         * Afficher toutes les visualisations
         * au démarrage.
         */
        rebuildChartsGrid(visualizations);

        /*
         * Activer la recherche.
         */
        installSearchListener();
    }

    /**
     * Création de la barre de recherche.
     */
    private JPanel createSearchBar(String message) {

        JPanel container =
                new JPanel(
                        new BorderLayout()
                );

        container.setOpaque(false);

        container.setBorder(
                new EmptyBorder(
                        6,
                        10,
                        2,
                        10
                )
        );

        JPanel fieldPanel =
                new JPanel(
                        new BorderLayout()
                );

        fieldPanel.setOpaque(true);

        fieldPanel.setBackground(
                DashboardTheme.SURFACE_2
        );

        fieldPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                DashboardTheme.BORDER_SUBTLE,
                                1
                        ),
                        new EmptyBorder(
                                0,
                                8,
                                0,
                                4
                        )
                )
        );

        JLabel searchIcon =
                new JLabel(
                        AllIcons.Actions.Find
                );

        searchIcon.setBorder(
                new EmptyBorder(
                        0,
                        0,
                        0,
                        4
                )
        );

        fieldPanel.add(
                searchIcon,
                BorderLayout.WEST
        );

        searchField =
                new JTextField();

        searchField.setOpaque(false);
        searchField.setBorder(null);

        searchField.setForeground(
                DashboardTheme.TEXT
        );

        searchField.setCaretColor(
                DashboardTheme.TEXT
        );

        searchField.setFont(
                searchField
                        .getFont()
                        .deriveFont(12f)
        );

        searchField.putClientProperty(
                "JTextField.placeholderText",
                message
        );

        fieldPanel.add(
                searchField,
                BorderLayout.CENTER
        );

        /*
         * Même largeur que les 3 cartes.
         */
        int width =
                COLUMNS * CARD_SIZE
                        + (COLUMNS - 1) * CARD_GAP;

        fieldPanel.setPreferredSize(
                new Dimension(
                        width,
                        30
                )
        );

        container.add(
                fieldPanel,
                BorderLayout.CENTER
        );

        return container;
    }

    /**
     * Active le filtrage lorsque le texte
     * de recherche change.
     */
    private void installSearchListener() {

        searchField
                .getDocument()
                .addDocumentListener(
                        new DocumentListener() {

                            @Override
                            public void insertUpdate(
                                    DocumentEvent e
                            ) {
                                filterVisualizations();
                            }

                            @Override
                            public void removeUpdate(
                                    DocumentEvent e
                            ) {
                                filterVisualizations();
                            }

                            @Override
                            public void changedUpdate(
                                    DocumentEvent e
                            ) {
                                filterVisualizations();
                            }
                        }
                );
    }

    /**
     * Filtre les visualisations en fonction
     * du nom et de la description.
     */
    private void filterVisualizations() {

        String query =
                searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        List<VisualizationItem> filtered =
                visualizations.stream()
                        .filter(item ->
                                query.isEmpty()
                                        || item.name
                                        .toLowerCase()
                                        .contains(query)

                                        || item.description
                                        .toLowerCase()
                                        .contains(query)
                        )
                        .toList();

        rebuildChartsGrid(filtered);
    }

    /**
     * Reconstruit la grille avec les visualisations
     * correspondant à la recherche.
     */
    private void rebuildChartsGrid(
            List<VisualizationItem> items
    ) {

        chartsGrid.removeAll();

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(
                        4,
                        4,
                        4,
                        4
                );

        gbc.weightx = 0;
        gbc.weighty = 0;

        gbc.fill =
                GridBagConstraints.NONE;

        int column = 0;
        int row = 0;

        for (VisualizationItem item : items) {

            gbc.gridx = column;
            gbc.gridy = row;

            gbc.anchor =
                    GridBagConstraints.CENTER;

            chartsGrid.add(
                    createVisualizationCard(item),
                    gbc
            );

            column++;

            if (column >= COLUMNS) {

                column = 0;
                row++;
            }
        }

        /*
         * Filler vertical pour pousser les cartes
         * vers le haut.
         */
        GridBagConstraints filler =
                new GridBagConstraints();

        filler.gridx = 0;
        filler.gridy = row + 1;

        filler.gridwidth = COLUMNS;

        filler.weightx = 1;
        filler.weighty = 1;

        filler.fill =
                GridBagConstraints.VERTICAL;

        chartsGrid.add(
                Box.createGlue(),
                filler
        );

        chartsGrid.revalidate();
        chartsGrid.repaint();

        /*
         * Revenir en haut après filtrage.
         */
        SwingUtilities.invokeLater(() -> {

            JScrollBar bar =
                    scrollPane.getVerticalScrollBar();

            bar.setValue(
                    bar.getMinimum()
            );
        });
    }

    public void addSelectionListener(VisualizationSelectionListener listener) {
        this.selectionListener = listener;
    }

    /**
     * Crée une carte de visualisation.
     */
    private JPanel createVisualizationCard(
            VisualizationItem item
    ) {

        JPanel card =
                new JPanel(
                        new BorderLayout()
                ) {

                    @Override
                    protected void paintComponent(
                            Graphics g
                    ) {

                        Graphics2D g2 =
                                (Graphics2D) g.create();

                        g2.setRenderingHint(
                                RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON
                        );

                        g2.setColor(
                                getBackground()
                        );

                        g2.fillRoundRect(
                                0,
                                0,
                                getWidth(),
                                getHeight(),
                                CARD_RADIUS,
                                CARD_RADIUS
                        );

                        g2.dispose();
                    }
                };

        card.setOpaque(false);

        card.setBackground(
                DashboardTheme.SURFACE_2
        );

        card.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        /*
         * Tooltip.
         */
        card.setToolTipText(
                "<html>" +
                        "<b>" +
                        item.name +
                        "</b><br>" +
                        "<span style='color:#888888'>" +
                        item.description +
                        "</span>" +
                        "</html>"
        );

        /*
         * Bordure normale.
         */
        card.setBorder(
                createNormalBorder()
        );

        /*
         * Chargement de l'image.
         */
        JLabel iconLabel =
                new JLabel(
                        loadVisualizationIcon(
                                item.imagePath,
                                46,
                                46
                        )
                );

        iconLabel.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        iconLabel.setVerticalAlignment(
                SwingConstants.CENTER
        );

        card.add(
                iconLabel,
                BorderLayout.CENTER
        );

        /*
         * Hover + clic.
         */
        card.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {

                        card.setBackground(
                                DashboardTheme.SURFACE_ACTIVE
                        );

                        card.setBorder(
                                createHoverBorder()
                        );

                        card.repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        card.setBackground(
                                DashboardTheme.SURFACE_2
                        );

                        card.setBorder(
                                createNormalBorder()
                        );

                        card.repaint();
                    }

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e
                    ) {

                        if (SwingUtilities
                                        .isLeftMouseButton(e)) {
                            if (selectionListener != null) {
                                selectionListener
                                        .onVisualizationSelected(
                                                item
                                        );
                            }
                        }
                    }
                }
        );

        /*
         * Taille fixe des cartes.
         */
        Dimension size =
                new Dimension(
                        CARD_SIZE,
                        CARD_SIZE
                );

        card.setPreferredSize(size);
        card.setMinimumSize(size);
        card.setMaximumSize(size);

        return card;
    }

    /**
     * Charge une image depuis les resources
     * et la redimensionne proprement.
     */
    private Icon loadVisualizationIcon(
            String path,
            int width,
            int height
    ) {

        java.net.URL resource =
                getClass().getResource(path);

        if (resource == null) {

            /*
             * Image introuvable :
             * on affiche une petite icône
             * d'erreur plutôt que de provoquer
             * une NullPointerException.
             */
            return AllIcons.General.Error;
        }

        ImageIcon original =
                new ImageIcon(resource);

        Image image =
                original
                        .getImage()
                        .getScaledInstance(
                                width,
                                height,
                                Image.SCALE_SMOOTH
                        );

        return new ImageIcon(image);
    }

    /**
     * Bordure normale d'une carte.
     */
    private javax.swing.border.Border createNormalBorder() {

        return  BorderFactory.createCompoundBorder(

                new RoundedBorder(
                        DashboardTheme.BORDER,
                        1,
                        CARD_RADIUS
                ),

                BorderFactory.createEmptyBorder(
                        3,
                        3,
                        3,
                        3
                )
        );
    }

    /**
     * Bordure au survol.
     */
    private javax.swing.border.Border createHoverBorder() {

        return BorderFactory.createCompoundBorder(

                new RoundedBorder(
                        DashboardTheme.ACCENT,
                        1,
                        CARD_RADIUS
                ),

                BorderFactory.createEmptyBorder(
                        3,
                        3,
                        3,
                        3
                )
        );
    }

    /**
     * Modèle d'une visualisation.
     *
     * Il n'y a plus de VisualizationType.
     * Une visualisation est simplement définie
     * par son nom, sa description, son image
     * et sa couleur.
     */
    public static class VisualizationItem {

        public final String name;
        public final String description;
        public final String imagePath;

        public VisualizationItem(
                String name,
                String description,
                String imagePath
        ) {

            this.name = name;
            this.description = description;
            this.imagePath = imagePath;
        }
    }

    public interface VisualizationSelectionListener {

        void onVisualizationSelected(
                VisualizationItem item
        );
    }
}