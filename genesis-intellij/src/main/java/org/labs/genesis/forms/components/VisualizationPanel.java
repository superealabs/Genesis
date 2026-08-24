package org.labs.genesis.forms.components;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.visuals.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
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

    private final List<VisualizationItem> visualizations = List.of(

            new VisualizationItem(
                    "Bar Chart Vertical",
                    "Vertical bar chart for comparing categories",
                    "/data_genesis/img/barchart-vertical.png",
                    VerticalBarChartRenderer.class
            ),

            new VisualizationItem(
                    "Bar Chart Horizontal",
                    "Horizontal bar chart for ranking data",
                    "/data_genesis/img/barchart-horizontal.png",
                    HorizontalBarChartRenderer.class
            ),

            new VisualizationItem(
                    "Pie Chart",
                    "Distribution of data in percentages",
                    "/data_genesis/img/piechart.png",
                    PieChartRenderer.class
            ),

            new VisualizationItem(
                    "Donut Chart",
                    "Pie chart with a hole in the center",
                    "/data_genesis/img/donutchart.png",
                    DonutChartRenderer.class
            ),

            new VisualizationItem(
                    "Line Chart",
                    "Trends over time or continuous data",
                    "/data_genesis/img/linechart.png",
                    LineChartRenderer.class
            ),

            new VisualizationItem(
                    "Gauge",
                    "Single value with min/max range",
                    "/data_genesis/img/jauge.png",
                    GaugeRenderer.class
            ),

            new VisualizationItem(
                    "KPI Card",
                    "Key performance indicator with value and trend",
                    "/data_genesis/img/kpi-card.png",
                    KpiRenderer.class
            ),

            new VisualizationItem(
                    "Table",
                    "Structured data in rows and columns",
                    "/data_genesis/img/table.png",
                    TableRenderer.class
            ),

            new VisualizationItem(
                    "Map",
                    "Geographical data visualization",
                    "/data_genesis/img/map.png",
                    MapRenderer.class
            ),

            new VisualizationItem(
                    "Scatter Plot",
                    "Visualize relationships between two numerical variables",
                    "/data_genesis/img/scatter-plot.png",
                    ScatterPlotRenderer.class
            )
    );

    public VisualizationPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel searchBar = createSearchBar("Search visualizations...");
        add(searchBar, BorderLayout.NORTH);

        chartsGrid = new JPanel(new GridBagLayout());
        chartsGrid.setOpaque(false);
        chartsGrid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        scrollPane = new JScrollPane(chartsGrid);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        rebuildChartsGrid(visualizations);
        installSearchListener();
    }

    private JPanel createSearchBar(String message) {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(6, 10, 2, 10));

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setOpaque(true);
        fieldPanel.setBackground(DashboardTheme.SURFACE_2);
        fieldPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.BORDER_SUBTLE, 1),
                new EmptyBorder(0, 8, 0, 4)
        ));

        JLabel searchIcon = new JLabel(AllIcons.Actions.Find);
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 4));
        fieldPanel.add(searchIcon, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.setOpaque(false);
        searchField.setBorder(null);
        searchField.setForeground(DashboardTheme.TEXT);
        searchField.setCaretColor(DashboardTheme.TEXT);
        searchField.setFont(searchField.getFont().deriveFont(12f));
        searchField.putClientProperty("JTextField.placeholderText", message);
        fieldPanel.add(searchField, BorderLayout.CENTER);

        int width = COLUMNS * CARD_SIZE + (COLUMNS - 1) * CARD_GAP;
        fieldPanel.setPreferredSize(new Dimension(width, 30));
        container.add(fieldPanel, BorderLayout.CENTER);
        return container;
    }

    private void installSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterVisualizations(); }
            @Override public void removeUpdate(DocumentEvent e) { filterVisualizations(); }
            @Override public void changedUpdate(DocumentEvent e) { filterVisualizations(); }
        });
    }

    private void filterVisualizations() {
        String query = searchField.getText().trim().toLowerCase();
        List<VisualizationItem> filtered = visualizations.stream()
                .filter(item -> query.isEmpty() ||
                        item.name.toLowerCase().contains(query) ||
                        item.description.toLowerCase().contains(query))
                .toList();
        rebuildChartsGrid(filtered);
    }

    private void rebuildChartsGrid(List<VisualizationItem> items) {
        chartsGrid.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;

        int col = 0, row = 0;
        for (VisualizationItem item : items) {
            gbc.gridx = col;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.CENTER;
            chartsGrid.add(createVisualizationCard(item), gbc);
            col++;
            if (col >= COLUMNS) { col = 0; row++; }
        }

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = row + 1;
        filler.gridwidth = COLUMNS;
        filler.weightx = 1;
        filler.weighty = 1;
        filler.fill = GridBagConstraints.VERTICAL;
        chartsGrid.add(Box.createGlue(), filler);

        chartsGrid.revalidate();
        chartsGrid.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMinimum());
        });
    }

    public void addSelectionListener(VisualizationSelectionListener listener) {
        this.selectionListener = listener;
    }

    private JPanel createVisualizationCard(VisualizationItem item) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), CARD_RADIUS, CARD_RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(DashboardTheme.SURFACE_2);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setToolTipText("<html><b>" + item.name + "</b><br><span style='color:#888888'>" + item.description + "</span></html>");
        card.setBorder(createNormalBorder());

        JLabel iconLabel = new JLabel(loadVisualizationIcon(item.imagePath, 46, 46));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);
        card.add(iconLabel, BorderLayout.CENTER);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(DashboardTheme.SURFACE_ACTIVE);
                card.setBorder(createHoverBorder());
                card.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(DashboardTheme.SURFACE_2);
                card.setBorder(createNormalBorder());
                card.repaint();
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && selectionListener != null) {
                    selectionListener.onVisualizationSelected(item);
                }
            }
        });

        Dimension size = new Dimension(CARD_SIZE, CARD_SIZE);
        card.setPreferredSize(size);
        card.setMinimumSize(size);
        card.setMaximumSize(size);
        return card;
    }

    private Icon loadVisualizationIcon(String path, int width, int height) {
        java.net.URL resource = getClass().getResource(path);
        if (resource == null) return AllIcons.General.Error;
        ImageIcon original = new ImageIcon(resource);
        Image image = original.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private javax.swing.border.Border createNormalBorder() {
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(DashboardTheme.BORDER, 1, CARD_RADIUS),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        );
    }

    private javax.swing.border.Border createHoverBorder() {
        return BorderFactory.createCompoundBorder(
                new RoundedBorder(DashboardTheme.ACCENT, 1, CARD_RADIUS),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)
        );
    }

    public static class VisualizationItem {

        public final String name;
        public final String description;
        public final String imagePath;

        public final Class<? extends VisualizationRenderer> rendererClass;

        public VisualizationItem(
                String name,
                String description,
                String imagePath,
                Class<? extends VisualizationRenderer> rendererClass
        ) {
            this.name = name;
            this.description = description;
            this.imagePath = imagePath;
            this.rendererClass = rendererClass;
        }
    }

    public interface VisualizationSelectionListener {
        void onVisualizationSelected(VisualizationItem item);
    }
}