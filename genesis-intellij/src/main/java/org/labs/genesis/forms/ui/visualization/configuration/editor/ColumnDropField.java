package org.labs.genesis.forms.ui.visualization.configuration.editor;

import com.intellij.icons.AllIcons;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.data.DataPanelTree;
import org.labs.genesis.forms.ui.data.model.ColumnData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.dnd.*;

public class ColumnDropField extends JPanel {

    @Getter
    private String columnName;

    private final JLabel iconLabel;
    private final JPanel textPanel;
    private final CardLayout textLayout;

    private static final String PLACEHOLDER_CARD = "PLACEHOLDER";
    private static final String VALUE_CARD = "VALUE";
    private final JLabel placeholderLabel;
    private final JLabel valueLabel;
    private final JButton clearButton;

    private boolean dragOver = false;
    private boolean dropSuccess = false;
    private float dropSuccessAlpha = 0f;

    public static final int HEIGHT = 32;
    private static final int RADIUS = 8;

    @Setter
    private java.util.function.Consumer<String> columnChangeListener;

    public ColumnDropField() {
        setLayout(new BorderLayout(8, 0));
        setOpaque(false);

        setPreferredSize(new Dimension(0, HEIGHT));
        setMinimumSize(new Dimension(0, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setToolTipText("Drag a column here");

        iconLabel = new JLabel(AllIcons.Nodes.C_plocal);
        iconLabel.setOpaque(false);
        iconLabel.setVisible(false);

        placeholderLabel = new JLabel("Drop column here...");
        placeholderLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
        placeholderLabel.setFont(DashboardTheme.getFont(11));
        placeholderLabel.setBorder(new EmptyBorder(0, 4, 0, 0));

        valueLabel = new JLabel();
        valueLabel.setForeground(DashboardTheme.TEXT);
        valueLabel.setFont(DashboardTheme.boldFont(12));
        valueLabel.setVisible(false);
        valueLabel.setBorder(new EmptyBorder(0, 4, 0, 0));

        clearButton = createClearButton();

        textLayout = new CardLayout();
        textPanel = new JPanel(textLayout);
        textPanel.setOpaque(false);
        textPanel.add(placeholderLabel, PLACEHOLDER_CARD);
        textPanel.add(valueLabel, VALUE_CARD);

        JPanel labelPanel = new JPanel(new BorderLayout(6, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(iconLabel, BorderLayout.WEST);
        labelPanel.add(textPanel, BorderLayout.CENTER);

        add(labelPanel, BorderLayout.CENTER);
        add(clearButton, BorderLayout.EAST);

        installDropTarget();
        installClickToClear();

        updateVisualState();
    }

    public void restoreValue(String value) {
        if (value != null && !value.trim().isEmpty()) {
            setColumn(value);
        } else {
            clearColumn();
        }
    }

    private JButton createClearButton() {
        JButton button = new JButton(AllIcons.Actions.Close);
        button.setPreferredSize(new Dimension(22, 22));
        button.setMinimumSize(new Dimension(22, 22));
        button.setMaximumSize(new Dimension(22, 22));
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(DashboardTheme.TEXT_SECONDARY);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Clear column");
        button.setVisible(false);
        button.setBorder(new EmptyBorder(0, 4, 0, 4));
        button.setMargin(new Insets(0, 4, 0, 4));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(DashboardTheme.ERROR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(DashboardTheme.TEXT_SECONDARY);
            }
        });

        button.addActionListener(e -> clearColumn());
        return button;
    }

    private void installDropTarget() {
        new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetAdapter() {

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                boolean supported =
                        dtde.isDataFlavorSupported(DataPanelTree.COLUMN_DATA_FLAVOR)
                                || dtde.isDataFlavorSupported(DataFlavor.stringFlavor);

                if (supported) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    setDragOver(true);
                } else {
                    dtde.rejectDrag();
                    setDragOver(false);
                }
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                // IMPORTANT :
                // Si l'utilisateur quitte le champ sans drop,
                // on remet immédiatement l'état normal.
                setDragOver(false);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                setDragOver(false);

                try {
                    boolean supported =
                            dtde.isDataFlavorSupported(DataPanelTree.COLUMN_DATA_FLAVOR)
                                    || dtde.isDataFlavorSupported(DataFlavor.stringFlavor);

                    if (!supported) {
                        dtde.rejectDrop();
                        return;
                    }

                    dtde.acceptDrop(DnDConstants.ACTION_COPY);

                    Transferable transferable = dtde.getTransferable();
                    String droppedColumn = null;

                    if (dtde.isDataFlavorSupported(
                            DataPanelTree.COLUMN_DATA_FLAVOR)) {

                        ColumnData columnData =
                                (ColumnData) transferable.getTransferData(
                                        DataPanelTree.COLUMN_DATA_FLAVOR
                                );

                        droppedColumn = columnData.name;

                    } else if (dtde.isDataFlavorSupported(
                            DataFlavor.stringFlavor)) {

                        droppedColumn = (String) transferable.getTransferData(
                                DataFlavor.stringFlavor
                        );
                    }

                    if (droppedColumn != null && !droppedColumn.isBlank()) {
                        setColumn(droppedColumn);
                        showDropSuccess();
                        dtde.dropComplete(true);
                    } else {
                        dtde.dropComplete(false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.dropComplete(false);

                } finally {
                    setDragOver(false);
                }
            }

        }, true);
    }

    private String extractColumnName(TransferHandler.TransferSupport support) throws Exception {
        Transferable transferable = support.getTransferable();

        if (support.isDataFlavorSupported(DataPanelTree.COLUMN_DATA_FLAVOR)) {
            ColumnData columnData = (ColumnData) transferable.getTransferData(DataPanelTree.COLUMN_DATA_FLAVOR);
            return columnData.name;
        }

        if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return (String) transferable.getTransferData(DataFlavor.stringFlavor);
        }

        return null;
    }

    private void setDragOver(boolean value) {
        if (dragOver == value) {
            return;
        }
        dragOver = value;
        updateVisualState();
    }

    private void showDropSuccess() {
        dropSuccess = true;
        dropSuccessAlpha = 1f;
        repaint();

        Timer fadeTimer = new Timer(50, null);
        fadeTimer.addActionListener(e -> {
            dropSuccessAlpha -= 0.1f;
            if (dropSuccessAlpha <= 0f) {
                dropSuccess = false;
                dropSuccessAlpha = 0f;
                fadeTimer.stop();
            }
            repaint();
        });
        fadeTimer.start();
    }

    private void updateVisualState() {
        boolean hasColumn = columnName != null && !columnName.isBlank();

        if (dragOver) {
            iconLabel.setVisible(false);
            placeholderLabel.setForeground(DashboardTheme.ACCENT);
            placeholderLabel.setText("Release to assign");
            clearButton.setVisible(false);
            textLayout.show(textPanel, PLACEHOLDER_CARD);
        } else if (hasColumn) {
            iconLabel.setVisible(true);
            valueLabel.setText(columnName);
            valueLabel.setForeground(DashboardTheme.TEXT);
            clearButton.setVisible(true);
            textLayout.show(textPanel, VALUE_CARD);
            setToolTipText("Click to clear: " + columnName);
        } else {
            iconLabel.setVisible(false);
            placeholderLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
            placeholderLabel.setText("Drop column here...");
            clearButton.setVisible(false);
            textLayout.show(textPanel, PLACEHOLDER_CARD);
            setToolTipText("Drag a column here");
        }

        // Un seul revalidate/repaint : revalidate() remonte de lui-même jusqu'à la
        // racine de validation correcte (JViewport le cas échéant). Pas besoin de
        // boucler manuellement sur getParent() ni de doubler avec invokeLater.
        revalidate();
        repaint();
    }

    private void installClickToClear() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)
                        && e.getClickCount() == 1
                        && columnName != null
                        && !columnName.isEmpty()) {
                    clearColumn();
                }
            }
        });
    }

    public void setColumn(String columnName) {
        if (columnName == null || columnName.isBlank()) {
            clearColumn();
            return;
        }

        this.columnName = columnName.trim();
        updateVisualState();

        // Notifié en dernier : tout effet de bord externe (persistance, mise à jour
        // d'un autre composant) se produit APRÈS que notre propre état visuel est
        // déjà finalisé et sa validation programmée.
        if (columnChangeListener != null) {
            columnChangeListener.accept(this.columnName);
        }
    }

    public void clearColumn() {
        this.columnName = null;
        updateVisualState();

        if (columnChangeListener != null) {
            columnChangeListener.accept(null);
        }
    }

    public String getTableName() {
        if (columnName == null || !columnName.contains(".")) {
            return null;
        }
        return columnName.split("\\.")[0];
    }

    public String getSimpleColumnName() {
        if (columnName == null) {
            return null;
        }
        if (columnName.contains(".")) {
            String[] parts = columnName.split("\\.");
            return parts.length > 1 ? parts[1] : parts[0];
        }
        return columnName;
    }

    public boolean hasTableName() {
        return columnName != null && columnName.contains(".");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            Color backgroundColor;
            Color borderColor;
            float borderWidth;
            float[] dashPattern = null;

            if (dropSuccess) {
                backgroundColor = withAlpha(DashboardTheme.SURFACE_2, (int) (200 * dropSuccessAlpha));
                borderColor = withAlpha(DashboardTheme.ACCENT, (int) (255 * dropSuccessAlpha));
                borderWidth = 2f;
            } else if (dragOver) {
                backgroundColor = DashboardTheme.SURFACE_2;
                borderColor = DashboardTheme.ACCENT;
                borderWidth = 2f;
            } else if (columnName != null && !columnName.isEmpty()) {
                backgroundColor = DashboardTheme.SURFACE_ACTIVE;
                borderColor = DashboardTheme.ACCENT;
                borderWidth = 1.5f;
            } else {
                backgroundColor = DashboardTheme.SURFACE_2;
                borderColor = DashboardTheme.BORDER_SUBTLE;
                borderWidth = 1f;
                dashPattern = new float[]{4f, 4f};
            }

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, width - 1, height - 1, RADIUS, RADIUS);
            g2.setColor(backgroundColor);
            g2.fill(shape);

            if (dashPattern != null) {
                g2.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dashPattern, 0f));
            } else {
                g2.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            g2.setColor(borderColor);
            g2.draw(shape);

            if (dragOver) {
                GradientPaint gradient = new GradientPaint(
                        0, 0, withAlpha(Color.WHITE, 40),
                        0, height, withAlpha(Color.WHITE, 0)
                );
                g2.setPaint(gradient);
                g2.fill(shape);
            }

        } finally {
            g2.dispose();
        }
    }

    private Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.clamp(alpha, 0, 255));
    }
}