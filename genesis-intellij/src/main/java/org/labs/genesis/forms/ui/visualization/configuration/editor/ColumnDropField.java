package org.labs.genesis.forms.ui.visualization.configuration.editor;

import com.intellij.icons.AllIcons;
import org.labs.genesis.forms.components.DataPanelTree;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ColumnDropField extends JPanel {

    private String columnName;

    private final JLabel iconLabel;
    private final JLabel placeholderLabel;
    private final JLabel valueLabel;
    private final JButton clearButton;

    private boolean dragOver = false;
    private boolean dropSuccess = false;
    private float dropSuccessAlpha = 0f;

    private static final int HEIGHT = 32;
    private static final int RADIUS = 8;

    private java.util.function.Consumer<String> columnChangeListener;

    public ColumnDropField() {
        setLayout(new BorderLayout(8, 0));
        setOpaque(false);

        setPreferredSize(new Dimension(0, HEIGHT));
        setMinimumSize(new Dimension(0, HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setAlignmentX(Component.LEFT_ALIGNMENT);

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

        JPanel labelPanel = new JPanel(new BorderLayout(6, 0));
        labelPanel.setOpaque(false);
        labelPanel.add(iconLabel, BorderLayout.WEST);
        labelPanel.add(placeholderLabel, BorderLayout.CENTER);
        labelPanel.add(valueLabel, BorderLayout.CENTER);

        add(labelPanel, BorderLayout.CENTER);
        add(clearButton, BorderLayout.EAST);

        installDropTarget();
        installClickToClear();

        updateVisualState();
    }

    /**
     * Restaure une valeur sauvegardée (utilisé lors du rechargement du panel)
     */
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
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                boolean supported =
                        support.isDataFlavorSupported(DataPanelTree.COLUMN_DATA_FLAVOR)
                                || support.isDataFlavorSupported(DataFlavor.stringFlavor);

                setDragOver(supported);
                return supported;
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    Transferable transferable = support.getTransferable();
                    String droppedColumn = null;

                    if (support.isDataFlavorSupported(DataPanelTree.COLUMN_DATA_FLAVOR)) {
                        DataPanelTree.ColumnData columnData =
                                (DataPanelTree.ColumnData) transferable.getTransferData(
                                        DataPanelTree.COLUMN_DATA_FLAVOR);
                        droppedColumn = columnData.name;
                    } else if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        droppedColumn = (String) transferable.getTransferData(
                                DataFlavor.stringFlavor);
                    }

                    if (droppedColumn != null && !droppedColumn.trim().isEmpty()) {
                        setColumn(droppedColumn);
                        showDropSuccess();
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setDragOver(false);
                }

                return false;
            }

            @Override
            public void exportDone(JComponent source, Transferable data, int action) {
                setDragOver(false);
            }
        });
    }

    private void setDragOver(boolean value) {
        if (dragOver == value) {
            return;
        }

        dragOver = value;
        updateVisualState();
        repaint();
    }

    private void showDropSuccess() {
        dropSuccess = true;
        dropSuccessAlpha = 1f;
        updateVisualState();
        repaint();

        Timer fadeTimer = new Timer(50, null);
        fadeTimer.addActionListener(e -> {
            dropSuccessAlpha -= 0.1f;
            if (dropSuccessAlpha <= 0f) {
                dropSuccess = false;
                dropSuccessAlpha = 0f;
                fadeTimer.stop();
            }
            updateVisualState();
            repaint();
        });
        fadeTimer.start();
    }

    private void updateVisualState() {
        if (dropSuccess) {
            placeholderLabel.setForeground(DashboardTheme.ACCENT);
            placeholderLabel.setText("Column added!");
        } else if (dragOver) {
            placeholderLabel.setForeground(DashboardTheme.ACCENT);
            placeholderLabel.setText("Release to assign");
        } else if (columnName != null && !columnName.isBlank()) {
            placeholderLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
            placeholderLabel.setText("");
            valueLabel.setVisible(true);
            iconLabel.setVisible(true);
        } else {
            placeholderLabel.setForeground(DashboardTheme.TEXT_SECONDARY);
            placeholderLabel.setText("Drop column here...");
            valueLabel.setVisible(false);
            iconLabel.setVisible(false);
        }
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

            @Override
            public void mouseEntered(MouseEvent e) {
                if (columnName != null && !columnName.isEmpty()) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });
    }

    public void setColumn(String columnName) {
        this.columnName = columnName;

        if (columnName != null && !columnName.trim().isEmpty()) {
            placeholderLabel.setVisible(false);
            valueLabel.setText(columnName);
            valueLabel.setVisible(true);
            iconLabel.setVisible(true);
            clearButton.setVisible(true);
        } else {
            clearColumn();
            return;
        }

        updateVisualState();

        if (columnChangeListener != null) {
            columnChangeListener.accept(columnName);
        }

        revalidate();
        repaint();
    }

    public void clearColumn() {
        this.columnName = null;

        placeholderLabel.setVisible(true);
        placeholderLabel.setText("Drop column here...");
        valueLabel.setVisible(false);
        valueLabel.setText("");
        iconLabel.setVisible(false);
        clearButton.setVisible(false);

        updateVisualState();

        if (columnChangeListener != null) {
            columnChangeListener.accept(null);
        }

        revalidate();
        repaint();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnChangeListener(java.util.function.Consumer<String> listener) {
        this.columnChangeListener = listener;
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
                backgroundColor = withAlpha(DashboardTheme.SURFACE_2, (int)(200 * dropSuccessAlpha));
                borderColor = withAlpha(DashboardTheme.ACCENT, (int)(255 * dropSuccessAlpha));
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
                g2.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                        10f, dashPattern, 0f));
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
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }
}