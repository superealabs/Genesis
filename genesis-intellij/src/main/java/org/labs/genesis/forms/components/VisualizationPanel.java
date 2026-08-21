package org.labs.genesis.forms.components;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class VisualizationPanel extends JPanel {

    private final JPanel chartsGrid;
    private final JScrollPane scrollPane;

    private JTextField searchField;

    private static final int CARD_SIZE = 64;
    private static final int COLUMNS = 3;

    private final List<VisualizationItem> visualizations = List.of(

            new VisualizationItem(
                    "Bar Chart Vertical",
                    "Vertical bar chart for comparing categories",
                    VisualizationType.BAR_VERTICAL,
                    new Color(66, 133, 244)
            ),

            new VisualizationItem(
                    "Bar Chart Horizontal",
                    "Horizontal bar chart for ranking data",
                    VisualizationType.BAR_HORIZONTAL,
                    new Color(52, 168, 83)
            ),

            new VisualizationItem(
                    "Pie Chart",
                    "Distribution of data in percentages",
                    VisualizationType.PIE,
                    new Color(234, 67, 53)
            ),

            new VisualizationItem(
                    "Donut Chart",
                    "Pie chart with a hole in the center",
                    VisualizationType.DONUT,
                    new Color(251, 188, 5)
            ),

            new VisualizationItem(
                    "Line Chart",
                    "Trends over time or continuous data",
                    VisualizationType.LINE,
                    new Color(156, 39, 176)
            ),

            new VisualizationItem(
                    "Scatter Plot",
                    "Relationship between two variables",
                    VisualizationType.SCATTER,
                    new Color(0, 188, 212)
            ),

            new VisualizationItem(
                    "Gauge",
                    "Single value with min/max range",
                    VisualizationType.GAUGE,
                    new Color(255, 152, 0)
            ),

            new VisualizationItem(
                    "Tree Map",
                    "Hierarchical data visualization",
                    VisualizationType.TREEMAP,
                    new Color(76, 175, 80)
            ),

            new VisualizationItem(
                    "KPI Card",
                    "Key performance indicator with value and trend",
                    VisualizationType.KPI,
                    new Color(34, 197, 94)
            ),

            new VisualizationItem(
                    "Table",
                    "Structured data in rows and columns",
                    VisualizationType.TABLE,
                    new Color(96, 125, 139)
            ),

            new VisualizationItem(
                    "Matrix",
                    "Relationship between two categorical variables",
                    VisualizationType.MATRIX,
                    new Color(233, 30, 99)
            ),

            new VisualizationItem(
                    "Slicer",
                    "Interactive data filtering",
                    VisualizationType.SLICER,
                    new Color(63, 81, 181)
            ),

            new VisualizationItem(
                    "Map",
                    "Geographical data visualization",
                    VisualizationType.MAP,
                    new Color(121, 85, 72)
            )
    );

    private void installSearchListener() {

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filterVisualizations();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filterVisualizations();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filterVisualizations();
                    }
                }
        );
    }

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

        searchField = new JTextField();

        searchField.setOpaque(false);
        searchField.setBorder(null);

        searchField.setForeground(
                DashboardTheme.TEXT
        );

        searchField.setCaretColor(
                DashboardTheme.TEXT
        );

        searchField.setFont(
                searchField.getFont().deriveFont(12f)
        );

        searchField.putClientProperty(
                "JTextField.placeholderText",
                message
        );

        fieldPanel.add(
                searchField,
                BorderLayout.CENTER
        );

        // Même largeur que les 3 cartes
        int cardGap = 8;

        int width =
                COLUMNS * CARD_SIZE
                        + (COLUMNS - 1) * cardGap;

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

    private void filterVisualizations() {

        String query =
                searchField.getText()
                        .trim()
                        .toLowerCase();

        List<VisualizationItem> filtered =
                visualizations.stream()
                        .filter(item ->
                                query.isEmpty()
                                        || item.name.toLowerCase().contains(query)
                                        || item.description.toLowerCase().contains(query)
                        )
                        .toList();

        rebuildChartsGrid(filtered);
    }

    private void rebuildChartsGrid(
            List<VisualizationItem> items
    ) {

        chartsGrid.removeAll();

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets =
                new Insets(4, 4, 4, 4);

        gbc.weightx = 0;
        gbc.weighty = 0;

        gbc.fill =
                GridBagConstraints.NONE;

        int column = 0;
        int row = 0;

        for (VisualizationItem item : items) {

            gbc.gridx = column;
            gbc.gridy = row;
            gbc.anchor = GridBagConstraints.CENTER;

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

        // Filler vertical pour pousser les cartes vers le haut
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

        // Revenir en haut après filtrage
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar =
                    scrollPane.getVerticalScrollBar();

            bar.setValue(
                    bar.getMinimum()
            );
        });
    }

    public VisualizationPanel() {

        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel searchBar =
                createSearchBar(
                        "Search tables or columns..."
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

        // Afficher toutes les visualisations au démarrage
        rebuildChartsGrid(visualizations);

        // Activer la recherche
        installSearchListener();
    }

    private JPanel createVisualizationCard(
            VisualizationItem item
    ) {

        JPanel card = new JPanel(
                new BorderLayout()
        );

        card.setOpaque(true);

        card.setBackground(
                DashboardTheme.SURFACE_2
        );

        card.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        card.setToolTipText(
                "<html>" +
                        "<b>" + item.name + "</b><br>" +
                        "<span style='color:#888888'>" +
                        item.description +
                        "</span>" +
                        "</html>"
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                DashboardTheme.BORDER,
                                1,
                                true
                        ),

                        BorderFactory.createEmptyBorder(
                                5,
                                5,
                                5,
                                5
                        )
                )
        );

        JLabel iconLabel = new JLabel(
                createChartIcon(
                        item.type,
                        item.color,
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

        card.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseEntered(
                            java.awt.event.MouseEvent e
                    ) {

                        card.setBackground(
                                DashboardTheme.SURFACE_ACTIVE
                        );

                        card.setBorder(
                                BorderFactory.createCompoundBorder(

                                        BorderFactory.createLineBorder(
                                                DashboardTheme.ACCENT,
                                                1,
                                                true
                                        ),

                                        BorderFactory.createEmptyBorder(
                                                5,
                                                5,
                                                5,
                                                5
                                        )
                                )
                        );

                        card.repaint();
                    }

                    @Override
                    public void mouseExited(
                            java.awt.event.MouseEvent e
                    ) {

                        card.setBackground(
                                DashboardTheme.SURFACE_2
                        );

                        card.setBorder(
                                BorderFactory.createCompoundBorder(

                                        BorderFactory.createLineBorder(
                                                DashboardTheme.BORDER,
                                                1,
                                                true
                                        ),

                                        BorderFactory.createEmptyBorder(
                                                5,
                                                5,
                                                5,
                                                5
                                        )
                                )
                        );

                        card.repaint();
                    }

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e
                    ) {

                        if (
                                SwingUtilities.isLeftMouseButton(e)
                        ) {

                            System.out.println(
                                    "Selected: " + item.name
                            );
                        }
                    }
                }
        );

        Dimension size = new Dimension(
                CARD_SIZE,
                CARD_SIZE
        );

        card.setPreferredSize(size);
        card.setMinimumSize(size);
        card.setMaximumSize(size);

        return card;
    }

    private Icon createChartIcon(
            VisualizationType type,
            Color color,
            int width,
            int height
    ) {

        BufferedImage image = new BufferedImage(
                width,
                height,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2 = image.createGraphics();

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );

        drawChartIcon(
                g2,
                type,
                color,
                width,
                height
        );

        g2.dispose();

        return new ImageIcon(image);
    }

    private void drawChartIcon(
            Graphics2D g2,
            VisualizationType type,
            Color color,
            int width,
            int height
    ) {

        int padding = 4;

        int w = width - 2 * padding;
        int h = height - 2 * padding;

        int x = padding;
        int y = padding;

        switch (type) {

            case BAR_VERTICAL ->
                    drawBarChartVertical(
                            g2, color, x, y, w, h
                    );

            case BAR_HORIZONTAL ->
                    drawBarChartHorizontal(
                            g2, color, x, y, w, h
                    );

            case PIE ->
                    drawPieChart(
                            g2, color, x, y, w, h
                    );

            case DONUT ->
                    drawDonutChart(
                            g2, color, x, y, w, h
                    );

            case LINE ->
                    drawLineChart(
                            g2, color, x, y, w, h
                    );

            case SCATTER ->
                    drawScatterPlot(
                            g2, color, x, y, w, h
                    );

            case GAUGE ->
                    drawGauge(
                            g2, color, x, y, w, h
                    );

            case TREEMAP ->
                    drawTreeMap(
                            g2, color, x, y, w, h
                    );

            case KPI ->
                    drawKpi(
                            g2, color, x, y, w, h
                    );

            case TABLE ->
                    drawTable(
                            g2, color, x, y, w, h
                    );

            case MATRIX ->
                    drawMatrix(
                            g2, color, x, y, w, h
                    );

            case SLICER ->
                    drawSlicer(
                            g2, color, x, y, w, h
                    );

            case MAP ->
                    drawMap(
                            g2, color, x, y, w, h
                    );
        }
    }

    private void drawBarChartVertical(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int bars = 4;
        int gap = 3;

        int barW =
                (w - (bars - 1) * gap) / bars;

        double[] heights = {
                0.4,
                0.7,
                0.5,
                0.9
        };

        for (int i = 0; i < bars; i++) {

            int barH =
                    (int) (h * heights[i]);

            int bx =
                    x + i * (barW + gap);

            int by =
                    y + h - barH;

            g2.setColor(
                    i == bars - 1
                            ? color
                            : getMutedColor(
                            color,
                            0.4f + i * 0.15f
                    )
            );

            g2.fillRoundRect(
                    bx,
                    by,
                    barW,
                    barH,
                    3,
                    3
            );
        }
    }

    private void drawBarChartHorizontal(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int bars = 4;
        int gap = 3;

        int barH =
                (h - (bars - 1) * gap) / bars;

        double[] widths = {
                0.9,
                0.5,
                0.7,
                0.4
        };

        for (int i = 0; i < bars; i++) {

            int barW =
                    (int) (w * widths[i]);

            int by =
                    y + i * (barH + gap);

            g2.setColor(
                    i == 0
                            ? color
                            : getMutedColor(
                            color,
                            0.4f + i * 0.15f
                    )
            );

            g2.fillRoundRect(
                    x,
                    by,
                    barW,
                    barH,
                    3,
                    3
            );
        }
    }

    private void drawPieChart(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int size =
                Math.min(w, h);

        int px =
                x + (w - size) / 2;

        int py =
                y + (h - size) / 2;

        g2.setColor(color);

        g2.fillArc(
                px,
                py,
                size,
                size,
                0,
                140
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.6f
                )
        );

        g2.fillArc(
                px,
                py,
                size,
                size,
                140,
                120
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.3f
                )
        );

        g2.fillArc(
                px,
                py,
                size,
                size,
                260,
                100
        );
    }

    private void drawDonutChart(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        drawPieChart(
                g2,
                color,
                x,
                y,
                w,
                h
        );

        int size =
                Math.min(w, h);

        int hole =
                size / 2;

        int hx =
                x + (w - hole) / 2;

        int hy =
                y + (h - hole) / 2;

        g2.setComposite(
                AlphaComposite.Clear
        );

        g2.fillOval(
                hx,
                hy,
                hole,
                hole
        );

        g2.setComposite(
                AlphaComposite.SrcOver
        );
    }

    private void drawLineChart(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int[] px = {
                x,
                x + w / 3,
                x + 2 * w / 3,
                x + w
        };

        int[] py = {
                y + (int) (h * 0.7),
                y + (int) (h * 0.3),
                y + (int) (h * 0.6),
                y + (int) (h * 0.1)
        };

        Path2D area =
                new Path2D.Float();

        area.moveTo(
                px[0],
                y + h
        );

        for (int i = 0; i < px.length; i++) {

            area.lineTo(
                    px[i],
                    py[i]
            );
        }

        area.lineTo(
                px[px.length - 1],
                y + h
        );

        area.closePath();

        g2.setColor(
                getMutedColor(
                        color,
                        0.2f
                )
        );

        g2.fill(area);

        g2.setColor(color);

        g2.setStroke(
                new BasicStroke(
                        2.0f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        for (int i = 0; i < px.length - 1; i++) {

            g2.drawLine(
                    px[i],
                    py[i],
                    px[i + 1],
                    py[i + 1]
            );
        }

        for (int i = 0; i < px.length; i++) {

            g2.fillOval(
                    px[i] - 2,
                    py[i] - 2,
                    4,
                    4
            );
        }
    }

    private void drawScatterPlot(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        g2.setColor(
                getMutedColor(
                        color,
                        0.25f
                )
        );

        g2.drawLine(
                x,
                y + h,
                x + w,
                y + h
        );

        g2.drawLine(
                x,
                y,
                x,
                y + h
        );

        float[][] points = {
                {0.15f, 0.80f},
                {0.30f, 0.65f},
                {0.40f, 0.45f},
                {0.55f, 0.50f},
                {0.70f, 0.25f},
                {0.85f, 0.15f}
        };

        for (int i = 0; i < points.length; i++) {

            int cx =
                    x + (int) (
                            w * points[i][0]
                    );

            int cy =
                    y + (int) (
                            h * points[i][1]
                    );

            g2.setColor(
                    i % 2 == 0
                            ? color
                            : getMutedColor(
                            color,
                            0.5f
                    )
            );

            g2.fillOval(
                    cx - 2,
                    cy - 2,
                    5,
                    5
            );
        }
    }

    private void drawGauge(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int size =
                Math.min(w, h);

        int gx =
                x + (w - size) / 2;

        int gy =
                y + (h - size) / 2 + 2;

        g2.setStroke(
                new BasicStroke(
                        3.5f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.2f
                )
        );

        g2.drawArc(
                gx,
                gy,
                size,
                size,
                180,
                -180
        );

        g2.setColor(color);

        g2.drawArc(
                gx,
                gy,
                size,
                size,
                180,
                -130
        );

        int cx =
                gx + size / 2;

        int cy =
                gy + size / 2;

        g2.setStroke(
                new BasicStroke(1.5f)
        );

        g2.drawLine(
                cx,
                cy,
                cx + (int) (
                        size * 0.3 *
                                Math.cos(
                                        Math.toRadians(-40)
                                )
                ),
                cy + (int) (
                        size * 0.3 *
                                Math.sin(
                                        Math.toRadians(-40)
                                )
                )
        );
    }

    private void drawTreeMap(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int gap = 2;

        int w1 =
                (int) (w * 0.60);

        int h1 =
                (int) (h * 0.60);

        g2.setColor(color);

        g2.fillRect(
                x,
                y,
                w1 - gap,
                h1 - gap
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.6f
                )
        );

        g2.fillRect(
                x + w1,
                y,
                w - w1,
                h1 - gap
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.4f
                )
        );

        g2.fillRect(
                x,
                y + h1,
                w1 - gap,
                h - h1
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.8f
                )
        );

        g2.fillRect(
                x + w1,
                y + h1,
                w - w1,
                h - h1
        );
    }

    private void drawKpi(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int arc = 6;

        g2.setColor(
                getMutedColor(
                        color,
                        0.12f
                )
        );

        g2.fillRoundRect(
                x,
                y,
                w,
                h,
                arc,
                arc
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.75f
                )
        );

        g2.drawRoundRect(
                x,
                y,
                w - 1,
                h - 1,
                arc,
                arc
        );

        int labelW =
                (int) (w * 0.42);

        g2.setColor(
                getMutedColor(
                        color,
                        0.45f
                )
        );

        g2.fillRoundRect(
                x + 5,
                y + 5,
                labelW,
                3,
                2,
                2
        );

        int valueW =
                (int) (w * 0.55);

        g2.setColor(color);

        g2.fillRoundRect(
                x + 5,
                y + 13,
                valueW,
                7,
                2,
                2
        );

        int tx =
                x + 5;

        int ty =
                y + h - 7;

        g2.setColor(
                getMutedColor(
                        color,
                        0.9f
                )
        );

        g2.setStroke(
                new BasicStroke(
                        1.5f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        Path2D trend =
                new Path2D.Float();

        trend.moveTo(
                tx,
                ty
        );

        trend.lineTo(
                tx + w * 0.20,
                ty - 2
        );

        trend.lineTo(
                tx + w * 0.42,
                ty + 1
        );

        trend.lineTo(
                tx + w * 0.68,
                ty - 5
        );

        g2.draw(trend);

        int arrowX =
                tx + (int) (w * 0.68);

        g2.drawLine(
                arrowX,
                ty - 5,
                arrowX - 4,
                ty - 2
        );

        g2.drawLine(
                arrowX,
                ty - 5,
                arrowX - 5,
                ty - 7
        );
    }

    private void drawTable(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int headerH =
                Math.max(6, h / 5);

        int rows = 4;

        int[] columnWidths = {
                (int) (w * 0.45),
                (int) (w * 0.25),
                w
                        - (int) (w * 0.45)
                        - (int) (w * 0.25)
        };

        g2.setColor(
                getMutedColor(
                        color,
                        0.08f
                )
        );

        g2.fillRoundRect(
                x,
                y,
                w,
                h,
                3,
                3
        );

        g2.setColor(color);

        g2.fillRoundRect(
                x,
                y,
                w,
                headerH,
                3,
                3
        );

        g2.setColor(
                getMutedColor(
                        color,
                        0.30f
                )
        );

        int currentX = x;

        for (int i = 0; i < columnWidths.length - 1; i++) {

            currentX += columnWidths[i];

            g2.drawLine(
                    currentX,
                    y,
                    currentX,
                    y + h
            );
        }

        int rowH =
                (h - headerH) / rows;

        for (int i = 0; i < rows; i++) {

            int rowY =
                    y + headerH + i * rowH;

            g2.drawLine(
                    x,
                    rowY,
                    x + w,
                    rowY
            );
        }

        int contentY =
                y + headerH + rowH / 2;

        for (int row = 0; row < rows; row++) {

            int textY =
                    contentY + row * rowH;

            int cx =
                    x + 3;

            for (int col = 0; col < 3; col++) {

                int available =
                        columnWidths[col] - 6;

                int lineW;

                if (col == 0) {

                    lineW =
                            (int) (available * 0.75);

                } else if (col == 1) {

                    lineW =
                            (int) (available * 0.55);

                } else {

                    lineW =
                            (int) (available * 0.65);
                }

                g2.setColor(
                        getMutedColor(
                                color,
                                row % 2 == 0
                                        ? 0.75f
                                        : 0.45f
                        )
                );

                g2.fillRoundRect(
                        cx,
                        textY - 1,
                        lineW,
                        2,
                        1,
                        1
                );

                cx += columnWidths[col];
            }
        }

        g2.setColor(
                getMutedColor(
                        color,
                        0.8f
                )
        );

        g2.drawRoundRect(
                x,
                y,
                w - 1,
                h - 1,
                3,
                3
        );
    }

    private void drawMatrix(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int size = 4;

        int labelSize =
                Math.max(
                        5,
                        Math.min(w, h) / 6
                );

        int gridW =
                w - labelSize;

        int gridH =
                h - labelSize;

        int cellW =
                gridW / size;

        int cellH =
                gridH / size;

        float[][] values = {
                {0.15f, 0.85f, 0.45f, 0.65f},
                {0.70f, 0.30f, 0.95f, 0.20f},
                {0.40f, 0.75f, 0.25f, 0.85f},
                {0.90f, 0.50f, 0.60f, 0.35f}
        };

        g2.setColor(
                getMutedColor(
                        color,
                        0.20f
                )
        );

        g2.fillRect(
                x,
                y,
                labelSize,
                labelSize
        );

        for (int col = 0; col < size; col++) {

            int cx =
                    x + labelSize + col * cellW;

            g2.setColor(
                    getMutedColor(
                            color,
                            0.35f + col * 0.1f
                    )
            );

            g2.fillRect(
                    cx,
                    y,
                    cellW - 1,
                    labelSize - 1
            );
        }

        for (int row = 0; row < size; row++) {

            int cy =
                    y + labelSize + row * cellH;

            g2.setColor(
                    getMutedColor(
                            color,
                            0.35f + row * 0.1f
                    )
            );

            g2.fillRect(
                    x,
                    cy,
                    labelSize - 1,
                    cellH - 1
            );

            for (int col = 0; col < size; col++) {

                int cx =
                        x + labelSize + col * cellW;

                float value =
                        values[row][col];

                int alpha =
                        45 + (int) (
                                210 * value
                        );

                g2.setColor(
                        new Color(
                                color.getRed(),
                                color.getGreen(),
                                color.getBlue(),
                                alpha
                        )
                );

                g2.fillRect(
                        cx,
                        cy,
                        cellW - 1,
                        cellH - 1
                );
            }
        }

        g2.setColor(
                getMutedColor(
                        color,
                        0.8f
                )
        );

        g2.drawRect(
                x,
                y,
                w - 1,
                h - 1
        );
    }

    private void drawSlicer(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        int items = 3;

        int gap = 3;

        int itemH =
                (h - (items - 1) * gap) / items;

        for (int i = 0; i < items; i++) {

            int iy =
                    y + i * (itemH + gap);

            boolean selected =
                    i == 1;

            g2.setColor(
                    selected
                            ? color
                            : getMutedColor(
                            color,
                            0.20f
                    )
            );

            g2.fillRoundRect(
                    x,
                    iy,
                    w,
                    itemH,
                    4,
                    4
            );

            g2.setColor(
                    getMutedColor(
                            color,
                            0.6f
                    )
            );

            g2.drawRoundRect(
                    x,
                    iy,
                    w - 1,
                    itemH - 1,
                    4,
                    4
            );

            if (selected) {

                g2.setColor(Color.WHITE);

                g2.fillOval(
                        x + 4,
                        iy + itemH / 2 - 2,
                        4,
                        4
                );
            }
        }
    }

    private void drawMap(
            Graphics2D g2,
            Color color,
            int x,
            int y,
            int w,
            int h
    ) {

        g2.setColor(
                getMutedColor(
                        color,
                        0.10f
                )
        );

        g2.fillRoundRect(
                x,
                y,
                w,
                h,
                5,
                5
        );

        Path2D land =
                new Path2D.Float();

        land.moveTo(
                x + w * 0.12,
                y + h * 0.20
        );

        land.curveTo(
                x + w * 0.25,
                y + h * 0.05,

                x + w * 0.45,
                y + h * 0.12,

                x + w * 0.52,
                y + h * 0.25
        );

        land.curveTo(
                x + w * 0.70,
                y + h * 0.12,

                x + w * 0.90,
                y + h * 0.25,

                x + w * 0.84,
                y + h * 0.42
        );

        land.curveTo(
                x + w * 0.96,
                y + h * 0.58,

                x + w * 0.70,
                y + h * 0.72,

                x + w * 0.65,
                y + h * 0.90
        );

        land.curveTo(
                x + w * 0.45,
                y + h * 0.82,

                x + w * 0.25,
                y + h * 0.95,

                x + w * 0.18,
                y + h * 0.70
        );

        land.curveTo(
                x + w * 0.03,
                y + h * 0.55,

                x + w * 0.08,
                y + h * 0.35,

                x + w * 0.12,
                y + h * 0.20
        );

        land.closePath();

        g2.setColor(
                getMutedColor(
                        color,
                        0.45f
                )
        );

        g2.fill(land);

        g2.setColor(color);

        g2.setStroke(
                new BasicStroke(
                        1.2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        g2.draw(land);

        g2.setColor(
                getMutedColor(
                        color,
                        0.65f
                )
        );

        Path2D border1 =
                new Path2D.Float();

        border1.moveTo(
                x + w * 0.28,
                y + h * 0.22
        );

        border1.curveTo(
                x + w * 0.42,
                y + h * 0.35,

                x + w * 0.38,
                y + h * 0.55,

                x + w * 0.55,
                y + h * 0.72
        );

        g2.draw(border1);

        Path2D border2 =
                new Path2D.Float();

        border2.moveTo(
                x + w * 0.58,
                y + h * 0.25
        );

        border2.curveTo(
                x + w * 0.70,
                y + h * 0.42,

                x + w * 0.65,
                y + h * 0.58,

                x + w * 0.78,
                y + h * 0.68
        );

        g2.draw(border2);

        drawMapMarker(
                g2,
                color,
                x + (int) (w * 0.38),
                y + (int) (h * 0.40)
        );

        drawMapMarker(
                g2,
                color,
                x + (int) (w * 0.68),
                y + (int) (h * 0.32)
        );

        drawMapMarker(
                g2,
                color,
                x + (int) (w * 0.58),
                y + (int) (h * 0.68)
        );
    }

    private void drawMapMarker(
            Graphics2D g2,
            Color color,
            int cx,
            int cy
    ) {

        int radius = 3;

        g2.setColor(
                getMutedColor(
                        color,
                        0.25f
                )
        );

        g2.fillOval(
                cx - 5,
                cy - 5,
                10,
                10
        );

        g2.setColor(color);

        g2.fillOval(
                cx - radius,
                cy - radius,
                radius * 2,
                radius * 2
        );

        g2.setColor(Color.WHITE);

        g2.fillOval(
                cx - 1,
                cy - 1,
                2,
                2
        );
    }

    private Color getMutedColor(
            Color base,
            float alphaFactor
    ) {

        int alpha =
                Math.min(
                        255,
                        Math.max(
                                0,
                                (int) (
                                        255 * alphaFactor
                                )
                        )
                );

        return new Color(
                base.getRed(),
                base.getGreen(),
                base.getBlue(),
                alpha
        );
    }

    public enum VisualizationType {

        BAR_VERTICAL,
        BAR_HORIZONTAL,
        PIE,
        DONUT,
        LINE,
        SCATTER,
        GAUGE,
        TREEMAP,
        KPI,
        TABLE,
        MATRIX,
        SLICER,
        MAP
    }

    public static class VisualizationItem {

        public final String name;
        public final String description;
        public final VisualizationType type;
        public final Color color;

        public VisualizationItem(
                String name,
                String description,
                VisualizationType type,
                Color color
        ) {

            this.name = name;
            this.description = description;
            this.type = type;
            this.color = color;
        }
    }
}