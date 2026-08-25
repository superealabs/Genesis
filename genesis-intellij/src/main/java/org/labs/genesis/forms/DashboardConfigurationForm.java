package org.labs.genesis.forms;

import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.labs.genesis.forms.components.*;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    // Panneau de visualisation (liste des graphiques)
    private VisualizationPanel visualizationPanel;
    private JPanel rightContentCardPanel;
    private CardLayout rightContentCardLayout;

    // ===== GESTION MULTIPAGE =====
    private final List<GridCanvas> pageCanvases = new ArrayList<>();
    private CardLayout canvasCardLayout;
    private int pageCounter = 0;

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
        sidebar.setBorder(new RoundedBackgroundBorder(DashboardTheme.SURFACE, DashboardTheme.BORDER,
                DashboardTheme.SIDEBAR_RADIUS, 1, false));
        sidebar.setLayout(new BorderLayout());
    }

    private RotatableLabel createLeftVerticalTitle(String text) {
        RotatableLabel label = new RotatableLabel(text);
        label.setRotation(RotatableLabel.Rotation.COUNTER_CLOCKWISE);
        label.setForeground(DashboardTheme.TEXT);
        label.setFont(DashboardTheme.boldFont(11));
        return label;
    }

    private RotatableLabel createRightVerticalTitle(String text) {
        RotatableLabel label = new RotatableLabel(text);
        label.setRotation(RotatableLabel.Rotation.COUNTER_CLOCKWISE);
        label.setForeground(DashboardTheme.TEXT);
        label.setFont(DashboardTheme.boldFont(11));
        return label;
    }

    private void rebuildLeftSidebar() {
        leftSidebar.removeAll();
        leftHorizontalTitle = createHorizontalTitle("DATA PANEL");
        leftSidebarTitle = createLeftVerticalTitle("DATA PANEL");
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
        rightSidebarTitle = createRightVerticalTitle("VISUALISATIONS");
        rightTopBar = createTopBar(false, rightCollapseButton, rightHorizontalTitle);

        rightCollapsedContent = new JPanel(new BorderLayout());
        rightCollapsedContent.setOpaque(false);
        rightCollapsedContent.setBorder(new EmptyBorder(0, 0, 0, 0));
        rightCollapsedContent.add(rightSidebarTitle, BorderLayout.NORTH);

        // Conteneur avec CardLayout pour basculer entre liste et configuration
        rightContentCardLayout = new CardLayout();
        rightContentCardPanel = new JPanel(rightContentCardLayout);
        rightContentCardPanel.setOpaque(false);
        rightContentCardPanel.setBorder(new EmptyBorder(8, 4, 8, 4));

        // Créer le VisualizationPanel et stocker la référence
        visualizationPanel = new VisualizationPanel();
        visualizationPanel.addSelectionListener(item -> {
            DashboardVisualComponent component = new DashboardVisualComponent(item, 4, 4);
            // Utilise le canvas actif (gridCanvas) pour ajouter le composant
            gridCanvas.addVisualComponent(component);
        });

        rightContentCardPanel.add(visualizationPanel, "list");

        // Carte "config" vide (remplie plus tard)
        JPanel emptyConfig = new JPanel();
        emptyConfig.setOpaque(false);
        rightContentCardPanel.add(emptyConfig, "config");

        rightCollapsedContent.add(rightContentCardPanel, BorderLayout.CENTER);
        rightSidebar.add(rightTopBar, BorderLayout.NORTH);
        rightSidebar.add(rightCollapsedContent, BorderLayout.CENTER);

        // Afficher la liste par défaut
        rightContentCardLayout.show(rightContentCardPanel, "list");
    }

    private void showVisualizationConfiguration(DashboardVisualComponent component) {
        if (component == null) return;

        VisualizationConfigurationPanel configPanel = new VisualizationConfigurationPanel(
                component,   // <- AJOUT : composant cible
                component.getVisualizationItem(),
                () -> gridCanvas.selectVisual(null),
                () -> {
                    DashboardVisualComponent selected = gridCanvas.getSelectedVisual();
                    if (selected != null) {
                        gridCanvas.removeVisualComponent(selected);
                        showVisualizationList();
                    }
                }
        );

        // Remplacer l'ancienne carte "config"
        Component old = rightContentCardPanel.getComponent(1);
        if (old != null) rightContentCardPanel.remove(old);
        rightContentCardPanel.add(configPanel, "config");
        rightContentCardLayout.show(rightContentCardPanel, "config");
        rightContentCardPanel.revalidate();
        rightContentCardPanel.repaint();
    }

    private void showVisualizationList() {
        // Forcer le panneau interne à revenir à la vue liste
        visualizationPanel.showVisualizations();
        rightContentCardLayout.show(rightContentCardPanel, "list");
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

        // === MODIFICATION MULTIPAGE : CardLayout au lieu de BorderLayout ===
        canvasCardLayout = new CardLayout();
        canvasPanel.setLayout(canvasCardLayout);
        // Le premier GridCanvas sera créé dans initializeTabs()
    }

    // === NOUVELLE MÉTHODE : crée un GridCanvas et l'ajoute au canvasPanel ===
    private GridCanvas createNewGridCanvas() {
        GridCanvas canvas = new GridCanvas();

        // Callback de sélection pour ce canvas
        canvas.setOnSelectionChanged(comp -> {
            if (comp != null) {
                showVisualizationConfiguration(comp);
            } else {
                showVisualizationList();
            }
        });

        // Envelopper dans un JScrollPane
        JScrollPane canvasScrollPane = new JScrollPane(canvas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        canvasScrollPane.setBorder(null);
        canvasScrollPane.setOpaque(false);
        canvasScrollPane.getViewport().setOpaque(false);
        canvasScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        // Identifiant unique pour le CardLayout
        String pageId = "page_" + (pageCounter++);
        canvasPanel.add(canvasScrollPane, pageId);

        // Ajouter à la liste
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
        tabsPanel.setBorder(new RoundedBackgroundBorder(DashboardTheme.SURFACE_2, DashboardTheme.BORDER, 10, 1, false));
    }

    private void initializeTabs() {
        tabsPanel.removeAll();
        pageCanvases.clear();
        pageCounter = 0;

        // Créer le premier canvas et le premier onglet
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
        // Créer un nouveau canvas pour ce nouvel onglet
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
        tab.setBorder(new RoundedBackgroundBorder(DashboardTheme.SURFACE_2, DashboardTheme.BORDER, 8, 1, false));
        tab.setPreferredSize(new Dimension(125, 30));

        JLabel label = new JLabel(title);
        label.setForeground(DashboardTheme.TEXT);
        label.setBorder(new EmptyBorder(0, 10, 0, 0));

        JButton closeButton = new JButton("×");
        closeButton.setPreferredSize(new Dimension(28, 28));
        closeButton.setFocusable(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setOpaque(false);
        closeButton.setForeground(DashboardTheme.TEXT_SECONDARY);
        closeButton.setFont(closeButton.getFont().deriveFont(Font.PLAIN, 16f));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> closeTab(tab));

        tab.add(label, BorderLayout.CENTER);
        tab.add(closeButton, BorderLayout.EAST);

        // === MODIFICATION MULTIPAGE : stocker le canvas et l'ID de page ===
        tab.putClientProperty("gridCanvas", canvas);
        tab.putClientProperty("pageId", getPageIdForCanvas(canvas));

        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectTab(tab);
            }
        };
        tab.addMouseListener(clickListener);
        label.addMouseListener(clickListener);

        return tab;
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

        // Récupérer le canvas associé et le retirer
        GridCanvas canvas = (GridCanvas) tab.getClientProperty("gridCanvas");
        String pageId = (String) tab.getClientProperty("pageId");
        if (canvas != null) {
            pageCanvases.remove(canvas);
            // Retirer le scrollpane parent du canvasPanel
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
                tab.setBorder(new RoundedBackgroundBorder(
                        isSelected ? DashboardTheme.SURFACE_ACTIVE : DashboardTheme.SURFACE_2,
                        DashboardTheme.BORDER, 8, 1, isSelected
                ));
            }
        }

        // Mettre à jour gridCanvas et afficher le bon panneau
        GridCanvas canvas = (GridCanvas) selected.getClientProperty("gridCanvas");
        String pageId = (String) selected.getClientProperty("pageId");
        if (canvas != null && pageId != null) {
            gridCanvas = canvas;
            canvasCardLayout.show(canvasPanel, pageId);

            gridCanvas.selectVisual(null);
        }

        tabsPanel.repaint();
    }

    private boolean isSelectedTab(JPanel tab) {
        return tab.getBorder() instanceof RoundedBackgroundBorder &&
                ((RoundedBackgroundBorder) tab.getBorder()).isActive();
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