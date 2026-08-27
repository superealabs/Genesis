package org.labs.genesis.forms;

import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.common.RotatableLabel;
import org.labs.genesis.forms.ui.common.RoundedBorder;
import org.labs.genesis.forms.ui.dashboard.DashboardVisualComponent;
import org.labs.genesis.forms.ui.dashboard.GridCanvas;
import org.labs.genesis.forms.ui.data.DataPanelTree;
import org.labs.genesis.forms.ui.visualization.VisualizationPanel;
import org.labs.genesis.forms.ui.visualization.configuration.VisualizationConfigurationPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DashboardConfigurationForm {

    private JPanel mainPanel;
    private JPanel leftSidebar;
    private JPanel centerArea;
    private JPanel rightSidebar;
    private JButton leftCollapseButton;
    private JButton rightCollapseButton;
    private RotatableLabel leftSidebarTitle;
    private RotatableLabel rightSidebarTitle;
    private JPanel canvasContainer;
    private JPanel canvasPanel;
    private JPanel tabsPanel;
    private GridCanvas gridCanvas;
    private JScrollPane tabsScrollPane;
    private JButton addTabButton;
    private JLabel titleLabel;

    private boolean leftSidebarCollapsed = false;
    private boolean rightSidebarCollapsed = false;

    private JPanel leftTopBar;
    private JPanel rightTopBar;
    private JLabel leftHorizontalTitle;
    private JLabel rightHorizontalTitle;
    private JPanel leftCollapsedContent;
    private JPanel rightCollapsedContent;

    private VisualizationPanel visualizationPanel;
    private JPanel rightContentCardPanel;
    private CardLayout rightContentCardLayout;

    private final List<GridCanvas> pageCanvases = new ArrayList<>();
    private CardLayout canvasCardLayout;
    private int pageCounter = 0;

    // Gestion du panel de configuration
    private VisualizationConfigurationPanel currentConfigPanel;
    private DashboardVisualComponent currentConfigComponent;
    private final String CONFIG_CARD = "config";
    private final String LIST_CARD = "list";

    public DashboardConfigurationForm() {
        configureMainPanel();
        configureCenterArea();
        configureSidebars();
        configureCanvas();
        configureTabs();
        installListeners();
        initializeTabs();
        refreshLayout();
    }

    private void configureMainPanel() {
        mainPanel.setOpaque(true);
        mainPanel.setBackground(DashboardTheme.BACKGROUND);
        mainPanel.setBorder(JBUI.Borders.empty(16));
        DashboardTheme.styleTitle(titleLabel);
        titleLabel.setFont(DashboardTheme.boldFont(18));
    }

    private void configureCenterArea() {
        centerArea.setOpaque(true);
        centerArea.setBackground(DashboardTheme.BACKGROUND);
        centerArea.setBorder(null);
    }

    private void configureSidebars() {
        configureSidebarContainer(leftSidebar);
        configureSidebarContainer(rightSidebar);
        configureCollapseButton(leftCollapseButton, "‹");
        configureCollapseButton(rightCollapseButton, "›");
        rebuildLeftSidebar();
        rebuildRightSidebar();
    }

    private void configureSidebarContainer(JPanel sidebar) {
        sidebar.setOpaque(false);
        sidebar.setBorder(new RoundedBorder(DashboardTheme.SURFACE, DashboardTheme.BORDER,
                DashboardTheme.SIDEBAR_RADIUS, 1, true));
        sidebar.setLayout(new BorderLayout());
    }

    private RotatableLabel createVerticalTitle(String text) {
        RotatableLabel label = new RotatableLabel(text);
        label.setRotation(RotatableLabel.Rotation.COUNTER_CLOCKWISE);
        label.setForeground(DashboardTheme.TEXT);
        label.setFont(DashboardTheme.boldFont(11));
        return label;
    }

    private void rebuildLeftSidebar() {
        leftSidebar.removeAll();
        leftHorizontalTitle = createHorizontalTitle("DATA PANEL");
        leftSidebarTitle = createVerticalTitle("DATA PANEL");
        leftTopBar = createTopBar(true, leftCollapseButton, leftHorizontalTitle);

        leftCollapsedContent = new JPanel(new BorderLayout());
        leftCollapsedContent.setOpaque(false);
        leftCollapsedContent.setBorder(new EmptyBorder(0, 0, 0, 0));
        leftCollapsedContent.add(leftSidebarTitle, BorderLayout.NORTH);

        JPanel dataWrapper = new JPanel(new BorderLayout());
        dataWrapper.setOpaque(false);
        dataWrapper.setBorder(new EmptyBorder(8, 4, 8, 4));
        dataWrapper.add(createDataPanelContent(), BorderLayout.CENTER);
        dataWrapper.setName("dataPanelWrapper");
        leftCollapsedContent.add(dataWrapper, BorderLayout.CENTER);

        leftSidebar.add(leftTopBar, BorderLayout.NORTH);
        leftSidebar.add(leftCollapsedContent, BorderLayout.CENTER);
    }

    private JPanel createDataPanelContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        panel.add(new DataPanelTree(), BorderLayout.CENTER);
        return panel;
    }

    private void rebuildRightSidebar() {
        rightSidebar.removeAll();
        rightHorizontalTitle = createHorizontalTitle("VISUALISATIONS");
        rightSidebarTitle = createVerticalTitle("VISUALISATIONS");
        rightTopBar = createTopBar(false, rightCollapseButton, rightHorizontalTitle);

        rightCollapsedContent = new JPanel(new BorderLayout());
        rightCollapsedContent.setOpaque(false);
        rightCollapsedContent.setBorder(new EmptyBorder(0, 0, 0, 0));
        rightCollapsedContent.add(rightSidebarTitle, BorderLayout.NORTH);

        rightContentCardLayout = new CardLayout();
        rightContentCardPanel = new JPanel(rightContentCardLayout);
        rightContentCardPanel.setOpaque(false);
        rightContentCardPanel.setBorder(new EmptyBorder(8, 4, 8, 4));

        visualizationPanel = new VisualizationPanel();
        visualizationPanel.addSelectionListener(item -> {
            DashboardVisualComponent component = new DashboardVisualComponent(item, 4, 4);
            gridCanvas.addVisualComponent(component);
        });

        rightContentCardPanel.add(visualizationPanel, LIST_CARD);

        // Panel vide pour la configuration (sera remplacé dynamiquement)
        JPanel emptyConfig = new JPanel();
        emptyConfig.setOpaque(false);
        rightContentCardPanel.add(emptyConfig, CONFIG_CARD);

        rightCollapsedContent.add(rightContentCardPanel, BorderLayout.CENTER);
        rightSidebar.add(rightTopBar, BorderLayout.NORTH);
        rightSidebar.add(rightCollapsedContent, BorderLayout.CENTER);

        rightContentCardLayout.show(rightContentCardPanel, LIST_CARD);
    }

    private void showVisualizationConfiguration(DashboardVisualComponent component) {
        if (component == null) {
            showVisualizationList();
            return;
        }

        // Si le composant a changé ou si le panel n'existe pas, recréer
        if (currentConfigComponent != component || currentConfigPanel == null) {
            // Supprimer l'ancien panel
            if (currentConfigPanel != null) {
                rightContentCardPanel.remove(currentConfigPanel);
            }

            currentConfigComponent = component;
            currentConfigPanel = new VisualizationConfigurationPanel(
                    component,
                    component.getVisualizationItem(),
                    this::onConfigBack,
                    this::onConfigDelete
            );

            // Ajouter le nouveau panel
            rightContentCardPanel.add(currentConfigPanel, CONFIG_CARD);
        }

        // Afficher le panel
        rightContentCardLayout.show(rightContentCardPanel, CONFIG_CARD);
        rightContentCardPanel.revalidate();
        rightContentCardPanel.repaint();
    }

    private void onConfigBack() {
        gridCanvas.selectVisual(null);
        showVisualizationList();
    }

    private void onConfigDelete() {
        DashboardVisualComponent selected = gridCanvas.getSelectedVisual();
        if (selected != null) {
            gridCanvas.removeVisualComponent(selected);
            currentConfigComponent = null;
            currentConfigPanel = null;
            showVisualizationList();
        }
    }

    private void showVisualizationList() {
        visualizationPanel.showVisualizations();
        rightContentCardLayout.show(rightContentCardPanel, LIST_CARD);
        rightContentCardPanel.revalidate();
        rightContentCardPanel.repaint();
    }

    private JPanel createTopBar(boolean left, JButton button, JLabel title) {
        JPanel topBar = new JPanel(new BorderLayout(4, 0));
        topBar.setOpaque(false);
        Dimension barSize = new Dimension(0, DashboardTheme.TOP_BAR_HEIGHT);
        topBar.setPreferredSize(barSize);
        topBar.setMinimumSize(barSize);
        topBar.setBorder(new EmptyBorder(4, 7, 4, 7));

        if (left) {
            topBar.add(button, BorderLayout.WEST);
            topBar.add(title, BorderLayout.CENTER);
        } else {
            topBar.add(title, BorderLayout.CENTER);
            topBar.add(button, BorderLayout.EAST);
        }
        return topBar;
    }

    private JLabel createHorizontalTitle(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(DashboardTheme.TEXT);
        label.setFont(DashboardTheme.boldFont(11));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void configureCollapseButton(JButton button, String text) {
        button.setText(text);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 18f));
        button.setForeground(DashboardTheme.TEXT);
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        Dimension size = new Dimension(DashboardTheme.COLLAPSE_BTN_SIZE, DashboardTheme.COLLAPSE_BTN_SIZE);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
    }

    private void configureCanvas() {
        canvasContainer.setOpaque(true);
        canvasContainer.setBackground(DashboardTheme.BACKGROUND);
        canvasContainer.setBorder(null);

        canvasPanel.setOpaque(false);
        canvasPanel.setBackground(DashboardTheme.CANVAS_BG);
        canvasPanel.removeAll();

        canvasCardLayout = new CardLayout();
        canvasPanel.setLayout(canvasCardLayout);
    }

    private GridCanvas createNewGridCanvas() {
        GridCanvas canvas = new GridCanvas();

        canvas.setOnSelectionChanged(comp -> {
            if (comp != null) {
                showVisualizationConfiguration(comp);
            } else {
                showVisualizationList();
                // Nettoyer la référence si plus sélectionné
                if (currentConfigComponent != null) {
                    currentConfigComponent = null;
                    currentConfigPanel = null;
                }
            }
        });

        JScrollPane canvasScrollPane = new JScrollPane(canvas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        canvasScrollPane.setBorder(null);
        canvasScrollPane.setOpaque(false);
        canvasScrollPane.getViewport().setOpaque(false);
        canvasScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        String pageId = "page_" + (pageCounter++);
        canvasPanel.add(canvasScrollPane, pageId);
        pageCanvases.add(canvas);

        return canvas;
    }

    private void configureTabs() {
        tabsScrollPane.setOpaque(false);
        tabsScrollPane.getViewport().setOpaque(false);
        tabsScrollPane.setBorder(null);
        tabsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        JScrollBar hBar = tabsScrollPane.getHorizontalScrollBar();
        hBar.setPreferredSize(new Dimension(0, 8));
        hBar.setUnitIncrement(20);

        tabsPanel.setOpaque(false);
        tabsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        tabsPanel.setBorder(new RoundedBorder(DashboardTheme.SURFACE_2, DashboardTheme.BORDER, 10, 1, true));
    }

    private void initializeTabs() {
        tabsPanel.removeAll();
        pageCanvases.clear();
        pageCounter = 0;

        GridCanvas firstCanvas = createNewGridCanvas();
        JPanel firstTab = createTab("Page 1", firstCanvas);
        tabsPanel.add(firstTab);
        tabsPanel.add(createAddButton());
        selectTab(firstTab);
        tabsPanel.revalidate();
        tabsPanel.repaint();
    }

    private JButton createAddButton() {
        JButton button = new JButton("+");
        button.setPreferredSize(new Dimension(32, 28));
        button.setFocusable(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(DashboardTheme.TEXT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 18f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText("New dashboard");
        button.addActionListener(e -> {
            int pageNumber = getTabCount() + 1;
            addTab("Page " + pageNumber);
        });
        return button;
    }

    private void addTab(String title) {
        GridCanvas newCanvas = createNewGridCanvas();
        JPanel tab = createTab(title, newCanvas);
        int addIndex = Math.max(0, tabsPanel.getComponentCount() - 1);
        tabsPanel.add(tab, addIndex);
        tabsPanel.revalidate();
        tabsPanel.repaint();
        selectTab(tab);

        SwingUtilities.invokeLater(() -> {
            JScrollBar hBar = tabsScrollPane.getHorizontalScrollBar();
            if (hBar.getMaximum() > hBar.getVisibleAmount()) {
                hBar.setValue(hBar.getMaximum() - hBar.getVisibleAmount());
            }
        });
    }

    private JPanel createTab(String title, GridCanvas canvas) {
        JPanel tab = new JPanel(new BorderLayout(4, 0));
        tab.setOpaque(false);
        tab.setBorder(new RoundedBorder(DashboardTheme.SURFACE_2, DashboardTheme.BORDER, 8, 1, true));
        tab.setPreferredSize(new Dimension(125, 30));

        JLabel label = createTabLabel(title, tab);
        JButton closeButton = createCloseButton(tab);

        tab.add(label, BorderLayout.CENTER);
        tab.add(closeButton, BorderLayout.EAST);

        tab.putClientProperty("gridCanvas", canvas);
        tab.putClientProperty("pageId", getPageIdForCanvas(canvas));
        tab.putClientProperty("titleLabel", label);

        return tab;
    }

    private JLabel createTabLabel(String title, JPanel tab) {
        JLabel label = new JLabel(title);
        label.setForeground(DashboardTheme.TEXT);
        label.setFont(DashboardTheme.boldFont(11));
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);

        attachTabMouseListener(label, tab);

        return label;
    }

    private JButton createCloseButton(JPanel tab) {
        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(28, 28));
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setOpaque(false);
        closeButton.setForeground(DashboardTheme.TEXT_SECONDARY);
        closeButton.setFont(closeButton.getFont().deriveFont(Font.PLAIN, 16f));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setToolTipText("Fermer");
        closeButton.addActionListener(e -> closeTab(tab));
        return closeButton;
    }

    private void attachTabMouseListener(JComponent component, JPanel tab) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 1) {
                        selectTab(tab);
                    } else if (e.getClickCount() == 2) {
                        startRenameTab(tab);
                    }
                }
            }
        };
        component.addMouseListener(mouseAdapter);
    }

    private void startRenameTab(JPanel tab) {
        JLabel label = (JLabel) tab.getClientProperty("titleLabel");
        if (label == null) return;

        String currentTitle = label.getText();

        JTextField textField = new JTextField(currentTitle);
        textField.setForeground(DashboardTheme.TEXT);
        textField.setBackground(DashboardTheme.SURFACE_ACTIVE);
        textField.setCaretColor(DashboardTheme.TEXT);
        textField.setFont(DashboardTheme.boldFont(11));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DashboardTheme.ACCENT, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 2)
        ));
        textField.setHorizontalAlignment(SwingConstants.LEFT);

        tab.remove(label);
        tab.add(textField, BorderLayout.CENTER);
        tab.revalidate();
        tab.repaint();

        textField.selectAll();
        textField.requestFocusInWindow();

        textField.addActionListener(e -> finishRenameTab(tab, textField));

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finishRenameTab(tab, textField);
            }
        });

        textField.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelRename"
        );
        textField.getActionMap().put("cancelRename", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRenameTab(tab, textField, label.getText());
            }
        });
    }

    private void finishRenameTab(JPanel tab, JTextField textField) {
        String newTitle = textField.getText().trim();
        if (newTitle.isEmpty()) {
            JLabel oldLabel = (JLabel) tab.getClientProperty("titleLabel");
            newTitle = oldLabel != null ? oldLabel.getText() : "Page";
        }

        updateTabTitle(tab, newTitle);
    }

    private void cancelRenameTab(JPanel tab, JTextField textField, String oldTitle) {
        updateTabTitle(tab, oldTitle);
    }

    private void updateTabTitle(JPanel tab, String newTitle) {
        // Supprimer le JTextField s'il existe
        for (Component comp : tab.getComponents()) {
            if (comp instanceof JTextField) {
                tab.remove(comp);
                break;
            }
        }

        // Récupérer ou créer le label
        JLabel label = (JLabel) tab.getClientProperty("titleLabel");
        if (label == null) {
            label = new JLabel();
            tab.putClientProperty("titleLabel", label);
        }

        label.setText(newTitle);
        label.setForeground(DashboardTheme.TEXT);
        label.setFont(DashboardTheme.boldFont(11));
        label.setBorder(new EmptyBorder(0, 10, 0, 0));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);

        // Supprimer les anciens écouteurs de souris
        for (MouseListener listener : label.getMouseListeners()) {
            label.removeMouseListener(listener);
        }

        // Réattacher les écouteurs de souris
        attachTabMouseListener(label, tab);

        // Ajouter le label au centre
        tab.add(label, BorderLayout.CENTER);
        tab.revalidate();
        tab.repaint();
    }

    private String getPageIdForCanvas(GridCanvas canvas) {
        int index = pageCanvases.indexOf(canvas);
        if (index >= 0) {
            return "page_" + index;
        }
        return "page_unknown";
    }

    private void closeTab(JPanel tab) {
        if (getTabCount() <= 1) return;
        boolean wasSelected = isSelectedTab(tab);
        int index = getTabIndex(tab);

        GridCanvas canvas = (GridCanvas) tab.getClientProperty("gridCanvas");
        String pageId = (String) tab.getClientProperty("pageId");
        if (canvas != null) {
            pageCanvases.remove(canvas);
            Component scrollPane = canvas.getParent();
            if (scrollPane != null) {
                canvasPanel.remove(scrollPane);
            }
        }

        tabsPanel.remove(tab);
        if (wasSelected) {
            int newIndex = Math.min(index, getTabCount() - 1);
            Component comp = getTabComponent(newIndex);
            if (comp instanceof JPanel) selectTab((JPanel) comp);
        }
        tabsPanel.revalidate();
        tabsPanel.repaint();
    }

    private void selectTab(JPanel selected) {
        for (Component comp : tabsPanel.getComponents()) {
            if (comp instanceof JPanel tab) {
                boolean isSelected = (tab == selected);
                tab.setBorder(new RoundedBorder(
                        isSelected ? DashboardTheme.SURFACE_ACTIVE : DashboardTheme.SURFACE_2,
                        DashboardTheme.BORDER, 8, 1, true
                ));
            }
        }

        GridCanvas canvas = (GridCanvas) selected.getClientProperty("gridCanvas");
        String pageId = (String) selected.getClientProperty("pageId");
        if (canvas != null && pageId != null) {
            gridCanvas = canvas;
            canvasCardLayout.show(canvasPanel, pageId);
            gridCanvas.selectVisual(null);
        }

        // Réinitialiser le panel de configuration lors du changement d'onglet
        currentConfigComponent = null;
        currentConfigPanel = null;

        tabsPanel.repaint();
    }

    private boolean isSelectedTab(JPanel tab) {
        return tab.getBorder() instanceof RoundedBorder &&
                ((RoundedBorder) tab.getBorder()).isFillBackground();
    }

    private int getTabIndex(JPanel tab) {
        int idx = 0;
        for (Component comp : tabsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                if (comp == tab) return idx;
                idx++;
            }
        }
        return -1;
    }

    private int getTabCount() {
        int count = 0;
        for (Component comp : tabsPanel.getComponents()) {
            if (comp instanceof JPanel) count++;
        }
        return count;
    }

    private Component getTabComponent(int index) {
        int cur = 0;
        for (Component comp : tabsPanel.getComponents()) {
            if (comp instanceof JPanel) {
                if (cur == index) return comp;
                cur++;
            }
        }
        return null;
    }

    private void installListeners() {
        leftCollapseButton.addActionListener(e -> {
            leftSidebarCollapsed = !leftSidebarCollapsed;
            refreshLayout();
        });
        rightCollapseButton.addActionListener(e -> {
            rightSidebarCollapsed = !rightSidebarCollapsed;
            refreshLayout();
        });
    }

    private void refreshLayout() {
        updateLeftSidebar();
        updateRightSidebar();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void updateLeftSidebar() {
        updateSidebarSize(leftSidebar, leftSidebarCollapsed, false);
        boolean collapsed = leftSidebarCollapsed;
        leftTopBar.setVisible(true);
        leftHorizontalTitle.setVisible(!collapsed);
        leftSidebarTitle.setVisible(collapsed);
        if (collapsed) {
            leftSidebarTitle.setRotation(RotatableLabel.Rotation.COUNTER_CLOCKWISE);
            leftCollapseButton.setText("›");
        } else {
            leftCollapseButton.setText("‹");
        }
        if (leftCollapsedContent.getComponentCount() > 1) {
            leftCollapsedContent.getComponent(1).setVisible(!collapsed);
        }
        leftSidebar.revalidate();
        leftSidebar.repaint();
    }

    private void updateRightSidebar() {
        updateSidebarSize(rightSidebar, rightSidebarCollapsed, true);
        boolean collapsed = rightSidebarCollapsed;
        rightTopBar.setVisible(true);
        rightHorizontalTitle.setVisible(!collapsed);
        rightSidebarTitle.setVisible(collapsed);
        if (collapsed) {
            rightSidebarTitle.setRotation(RotatableLabel.Rotation.COUNTER_CLOCKWISE);
            rightCollapseButton.setText("‹");
        } else {
            rightCollapseButton.setText("›");
        }
        if (rightCollapsedContent.getComponentCount() > 1) {
            rightCollapsedContent.getComponent(1).setVisible(!collapsed);
        }
        rightSidebar.revalidate();
        rightSidebar.repaint();
    }

    private void updateSidebarSize(JPanel sidebar, boolean collapsed, boolean isExtended) {
        if (collapsed) {
            Dimension size = new Dimension(DashboardTheme.COLLAPSED_WIDTH, 0);
            sidebar.setPreferredSize(size);
            sidebar.setMinimumSize(size);
            sidebar.setMaximumSize(size);
        } else {
            int width = isExtended ? DashboardTheme.EXPANDED_WIDTH_EXTEND : DashboardTheme.EXPANDED_WIDTH;
            sidebar.setPreferredSize(new Dimension(width, 0));
            sidebar.setMinimumSize(new Dimension(DashboardTheme.MIN_WIDTH, 100));
            sidebar.setMaximumSize(new Dimension(DashboardTheme.MAX_WIDTH, Integer.MAX_VALUE));
        }
        sidebar.setVisible(true);
    }
}