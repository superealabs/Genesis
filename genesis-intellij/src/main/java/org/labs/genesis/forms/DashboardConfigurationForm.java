package org.labs.genesis.forms;

import com.intellij.util.ui.JBUI;
import lombok.Getter;
import org.labs.genesis.forms.components.*;
import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

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
    private JScrollPane tabsScrollPane;
    private JButton addTabButton;

    private JLabel titleLabel;

    private boolean leftSidebarCollapsed = false;
    private boolean rightSidebarCollapsed = false;

    // =========================================================
    // DYNAMIC SIDEBAR COMPONENTS
    // =========================================================

    private JPanel leftTopBar;
    private JPanel rightTopBar;

    private JLabel leftHorizontalTitle;
    private JLabel rightHorizontalTitle;

    private JPanel leftCollapsedContent;
    private JPanel rightCollapsedContent;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

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

    // =========================================================
    // MAIN PANEL
    // =========================================================

    private void configureMainPanel() {

        mainPanel.setOpaque(true);

        mainPanel.setBackground(
                DashboardTheme.BACKGROUND
        );

        mainPanel.setBorder(
                JBUI.Borders.empty(16)
        );

        DashboardTheme.styleTitle(titleLabel);
        titleLabel.setFont(DashboardTheme.boldFont(18));
    }

    // =========================================================
    // CENTER AREA
    // =========================================================

    private void configureCenterArea() {

        centerArea.setOpaque(true);

        centerArea.setBackground(
                DashboardTheme.BACKGROUND
        );

        centerArea.setBorder(null);
    }

    // =========================================================
    // SIDEBARS
    // =========================================================

    void configureSidebars() {

        configureSidebarContainer(
                leftSidebar
        );

        configureSidebarContainer(
                rightSidebar
        );

        configureCollapseButton(
                leftCollapseButton,
                "‹"
        );

        configureCollapseButton(
                rightCollapseButton,
                "›"
        );

        rebuildLeftSidebar();

        rebuildRightSidebar();
    }

    private void configureSidebarContainer(
            JPanel sidebar
    ) {

        sidebar.setOpaque(false);

        sidebar.setBorder(
                new RoundedBackgroundBorder(
                        DashboardTheme.SURFACE,
                        DashboardTheme.BORDER,
                        DashboardTheme.SIDEBAR_RADIUS,
                        1,
                        false
                )
        );

        sidebar.setLayout(
                new BorderLayout()
        );
    }

    private RotatableLabel createLeftVerticalTitle(
            String text
    ) {

        RotatableLabel label =
                new RotatableLabel();

        label.setText(text);

        label.setRotation(
                RotatableLabel.Rotation.COUNTER_CLOCKWISE
        );

        label.setForeground(DashboardTheme.TEXT);

        label.setFont(
                DashboardTheme.boldFont(11)
        );

        return label;
    }

    private RotatableLabel createRightVerticalTitle(
            String text
    ) {

        RotatableLabel label =
                new RotatableLabel();

        label.setText(text);

        label.setRotation(
                RotatableLabel.Rotation.COUNTER_CLOCKWISE
        );

        label.setForeground(DashboardTheme.TEXT);

        label.setFont(
                DashboardTheme.boldFont(11)
        );

        return label;
    }

    // =========================================================
    // LEFT SIDEBAR
    // =========================================================

    private void rebuildLeftSidebar() {
        leftSidebar.removeAll();

        // HORIZONTAL TITLE
        leftHorizontalTitle = createHorizontalTitle("DATA PANEL");

        // VERTICAL TITLE (pour l'état collapsed)
        leftSidebarTitle = createLeftVerticalTitle("DATA PANEL");

        // TOP BAR
        leftTopBar = createTopBar(true, leftCollapseButton, leftHorizontalTitle);

        // CONTENEUR PRINCIPAL
        leftCollapsedContent = new JPanel(new BorderLayout());
        leftCollapsedContent.setOpaque(false);
        leftCollapsedContent.setBorder(new EmptyBorder(0, 0, 0, 0));

        // On ajoute le titre vertical
        leftCollapsedContent.add(leftSidebarTitle, BorderLayout.NORTH);

        // On ajoute le data panel dans un conteneur avec padding
        JPanel dataWrapper = new JPanel(new BorderLayout());
        dataWrapper.setOpaque(false);
        dataWrapper.setBorder(new EmptyBorder(8, 4, 8, 4));
        dataWrapper.add(createDataPanelContent(), BorderLayout.CENTER);

        // On stocke le dataWrapper avec un nom pour le retrouver
        dataWrapper.setName("dataPanelWrapper");
        leftCollapsedContent.add(dataWrapper, BorderLayout.CENTER);

        // ADD TO SIDEBAR
        leftSidebar.add(leftTopBar, BorderLayout.NORTH);
        leftSidebar.add(leftCollapsedContent, BorderLayout.CENTER);
    }

    // Nouvelle méthode pour créer le contenu du data panel
    private JPanel createDataPanelContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        DataPanelTree dataTree = new DataPanelTree();
        panel.add(dataTree, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // RIGHT SIDEBAR
    // =========================================================

    private void rebuildRightSidebar() {
        rightSidebar.removeAll();

        // HORIZONTAL TITLE
        rightHorizontalTitle = createHorizontalTitle("VISUALISATIONS");

        // VERTICAL TITLE
        rightSidebarTitle = createRightVerticalTitle("VISUALISATIONS");

        // TOP BAR
        rightTopBar = createTopBar(false, rightCollapseButton, rightHorizontalTitle);

        // CONTENEUR PRINCIPAL
        rightCollapsedContent = new JPanel(new BorderLayout());
        rightCollapsedContent.setOpaque(false);
        rightCollapsedContent.setBorder(new EmptyBorder(0, 0, 0, 0));

        // On ajoute le titre vertical
        rightCollapsedContent.add(rightSidebarTitle, BorderLayout.NORTH);

        // On ajoute le panel de visualisations dans un conteneur avec padding
        JPanel vizWrapper = new JPanel(new BorderLayout());
        vizWrapper.setOpaque(false);
        vizWrapper.setBorder(new EmptyBorder(8, 4, 8, 4));
        vizWrapper.add(createVisualizationPanelContent(), BorderLayout.CENTER);

        // On stocke le wrapper avec un nom pour le retrouver
        vizWrapper.setName("visualizationPanelWrapper");
        rightCollapsedContent.add(vizWrapper, BorderLayout.CENTER);

        // ADD TO SIDEBAR
        rightSidebar.add(rightTopBar, BorderLayout.NORTH);
        rightSidebar.add(rightCollapsedContent, BorderLayout.CENTER);
    }

    // =========================================================
    // TOP BAR
    // =========================================================

    private JPanel createTopBar(
            boolean left,
            JButton button,
            JLabel title
    ) {

        JPanel topBar =
                new JPanel(
                        new BorderLayout(
                                4,
                                0
                        )
                );

        topBar.setOpaque(false);

        topBar.setPreferredSize(
                new Dimension(
                        0,
                        DashboardTheme.TOP_BAR_HEIGHT
                )
        );

        topBar.setMinimumSize(
                new Dimension(
                        0,
                        DashboardTheme.TOP_BAR_HEIGHT
                )
        );

        topBar.setBorder(
                new EmptyBorder(
                        4,
                        7,
                        4,
                        7
                )
        );

        /*
         * Gauche :
         *
         * ‹  DATA PANEL
         *
         * Droite :
         *
         * VISUALISATIONS  ›
         */
        if (left) {

            topBar.add(
                    button,
                    BorderLayout.WEST
            );

            topBar.add(
                    title,
                    BorderLayout.CENTER
            );

        } else {

            topBar.add(
                    title,
                    BorderLayout.CENTER
            );

            topBar.add(
                    button,
                    BorderLayout.EAST
            );
        }

        return topBar;
    }

    // =========================================================
    // HORIZONTAL TITLE
    // =========================================================

    private JLabel createHorizontalTitle(
            String text
    ) {

        JLabel label =
                new JLabel(text);

        label.setForeground(
                DashboardTheme.TEXT
        );

        label.setFont(
                DashboardTheme.boldFont(11)
        );

        label.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        label.setVerticalAlignment(
                SwingConstants.CENTER
        );

        return label;
    }

    // =========================================================
    // COLLAPSE BUTTON
    // =========================================================

    private void configureCollapseButton(
            JButton button,
            String text
    ) {

        button.setText(text);

        button.setFont(
                button.getFont().deriveFont(
                        Font.BOLD,
                        18f
                )
        );

        button.setForeground(
                DashboardTheme.TEXT
        );

        button.setFocusable(false);

        button.setBorderPainted(false);

        button.setContentAreaFilled(false);

        button.setOpaque(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setMargin(
                new Insets(
                        0,
                        0,
                        0,
                        0
                )
        );

        /*
         * Taille contrôlée pour éviter que
         * ‹ / › ne déborde du haut.
         */
        button.setPreferredSize(
                new Dimension(
                        DashboardTheme.COLLAPSE_BTN_SIZE,
                        DashboardTheme.COLLAPSE_BTN_SIZE
                )
        );

        button.setMinimumSize(
                new Dimension(
                        DashboardTheme.COLLAPSE_BTN_SIZE,
                        DashboardTheme.COLLAPSE_BTN_SIZE
                )
        );

        button.setMaximumSize(
                new Dimension(
                        DashboardTheme.COLLAPSE_BTN_SIZE,
                        DashboardTheme.COLLAPSE_BTN_SIZE
                )
        );

        button.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        button.setVerticalAlignment(
                SwingConstants.CENTER
        );
    }

    // =========================================================
    // CANVAS
    // =========================================================

    private void configureCanvas() {

        /*
         * Le conteneur du canvas doit avoir exactement
         * la couleur du fond général.
         */
        canvasContainer.setOpaque(true);

        canvasContainer.setBackground(
                DashboardTheme.BACKGROUND
        );

        canvasContainer.setBorder(null);

        /*
         * Le panel intermédiaire reste transparent.
         */
        canvasPanel.setOpaque(false);

        canvasPanel.setBackground(
                DashboardTheme.CANVAS_BG
        );

        canvasPanel.removeAll();

        GridCanvas gridCanvas =
                new GridCanvas();

        canvasPanel.setLayout(new BorderLayout());

        JScrollPane canvasScrollPane = new JScrollPane(
                gridCanvas,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        canvasScrollPane.setBorder(null);
        canvasScrollPane.setOpaque(false);
        canvasScrollPane.getViewport().setOpaque(false);

        canvasScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        canvasPanel.add(
                canvasScrollPane,
                BorderLayout.CENTER
        );

        canvasPanel.revalidate();
        canvasPanel.repaint();
    }

    // =========================================================
    // TABS
    // =========================================================

    private void configureTabs() {

        tabsScrollPane.setOpaque(false);

        tabsScrollPane.getViewport().setOpaque(false);

        tabsScrollPane.setBorder(null);

        tabsScrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        tabsScrollPane.setVerticalScrollBarPolicy(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
        );

        JScrollBar horizontalBar = tabsScrollPane.getHorizontalScrollBar();

        horizontalBar.setPreferredSize(
                new Dimension(0, 8)
        );

        horizontalBar.setUnitIncrement(20);

        tabsScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        tabsPanel.setOpaque(false);

        tabsPanel.setLayout(
                new FlowLayout(
                        FlowLayout.LEFT,
                        5,
                        5
                )
        );

        tabsPanel.setBorder(
                new RoundedBackgroundBorder(
                        DashboardTheme.SURFACE_2,
                        DashboardTheme.BORDER,
                        10,
                        1,
                        false
                )
        );
    }

    // =========================================================
    // INITIAL TABS
    // =========================================================

    private void initializeTabs() {

        tabsPanel.removeAll();

        addTab("Page 1");

        tabsPanel.add(
                createAddButton()
        );

        tabsPanel.revalidate();
        tabsPanel.repaint();
    }

    private JButton createAddButton() {

        JButton button =
                new JButton("+");

        button.setPreferredSize(
                new Dimension(
                        32,
                        28
                )
        );

        button.setFocusable(false);

        button.setBorderPainted(false);

        button.setContentAreaFilled(false);

        button.setOpaque(false);

        button.setForeground(
                DashboardTheme.TEXT
        );

        button.setFont(
                button.getFont().deriveFont(
                        Font.BOLD,
                        18f
                )
        );

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        button.setToolTipText(
                "New dashboard"
        );

        button.addActionListener(
                e -> {

                    int pageNumber =
                            getTabCount() + 1;

                    addTab(
                            "Page " + pageNumber
                    );
                }
        );

        return button;
    }

    private JPanel createVisualizationPanelContent() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);

        panel.setBorder(
                new EmptyBorder(
                        8,
                        8,
                        8,
                        8
                )
        );

        VisualizationPanel vizPanel =
                new VisualizationPanel();

        panel.add(
                vizPanel,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =========================================================
    // ADD TAB
    // =========================================================

    private void addTab(String title) {

        JPanel tab = createTab(title);

        int addButtonIndex =
                Math.max(
                        0,
                        tabsPanel.getComponentCount() - 1
                );

        tabsPanel.add(
                tab,
                addButtonIndex
        );

        tabsPanel.revalidate();
        tabsPanel.repaint();

        selectTab(tab);

        // Attendre que Swing recalcule les dimensions
        SwingUtilities.invokeLater(() -> {

            JScrollBar horizontalBar =
                    tabsScrollPane.getHorizontalScrollBar();

            // Vérifie s'il y a réellement quelque chose à faire défiler
            if (horizontalBar.getMaximum()
                    > horizontalBar.getVisibleAmount()) {

                horizontalBar.setValue(
                        horizontalBar.getMaximum()
                                - horizontalBar.getVisibleAmount()
                );
            }
        });
    }

    // =========================================================
    // CREATE TAB
    // =========================================================

    private JPanel createTab(
            String title
    ) {

        JPanel tab =
                new JPanel(
                        new BorderLayout(4, 0)
                );

        tab.setOpaque(false);

        tab.setBorder(
                new RoundedBackgroundBorder(
                        DashboardTheme.SURFACE_2,
                        DashboardTheme.BORDER,
                        8,
                        1,
                        false
                )
        );

        tab.setPreferredSize(
                new Dimension(
                        125,
                        30
                )
        );

        JLabel label =
                new JLabel(title);

        label.setForeground(
                DashboardTheme.TEXT
        );

        label.setBorder(
                new EmptyBorder(
                        0,
                        10,
                        0,
                        0
                )
        );

        JButton closeButton =
                new JButton("×");

        closeButton.setPreferredSize(
                new Dimension(
                        28,
                        28
                )
        );

        closeButton.setFocusable(false);

        closeButton.setBorderPainted(false);

        closeButton.setContentAreaFilled(false);

        closeButton.setOpaque(false);

        closeButton.setForeground(
                DashboardTheme.TEXT_SECONDARY
        );

        closeButton.setFont(
                closeButton.getFont().deriveFont(
                        Font.PLAIN,
                        16f
                )
        );

        closeButton.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        closeButton.addActionListener(
                e -> closeTab(tab)
        );

        tab.add(
                label,
                BorderLayout.CENTER
        );

        tab.add(
                closeButton,
                BorderLayout.EAST
        );

        MouseAdapter clickListener =
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e
                    ) {

                        selectTab(tab);
                    }
                };

        tab.addMouseListener(
                clickListener
        );

        label.addMouseListener(
                clickListener
        );

        return tab;
    }

    // =========================================================
    // CLOSE TAB
    // =========================================================

    private void closeTab(
            JPanel tab
    ) {

        if (getTabCount() <= 1) {
            return;
        }

        boolean wasSelected =
                isSelectedTab(tab);

        int index =
                getTabIndex(tab);

        tabsPanel.remove(tab);

        if (wasSelected) {

            int newIndex =
                    Math.min(
                            index,
                            getTabCount() - 1
                    );

            Component component =
                    getTabComponent(
                            newIndex
                    );

            if (component instanceof JPanel) {

                selectTab(
                        (JPanel) component
                );
            }
        }

        tabsPanel.revalidate();

        tabsPanel.repaint();
    }

    // =========================================================
    // SELECT TAB
    // =========================================================

    private void selectTab(
            JPanel selected
    ) {

        for (
                Component component :
                tabsPanel.getComponents()
        ) {

            if (!(component instanceof JPanel)) {
                continue;
            }

            JPanel tab =
                    (JPanel) component;

            if (tab == selected) {

                tab.setBorder(
                        new RoundedBackgroundBorder(
                                DashboardTheme.SURFACE_ACTIVE,
                                DashboardTheme.BORDER,
                                8,
                                1,
                                true
                        )
                );

            } else {

                tab.setBorder(
                        new RoundedBackgroundBorder(
                                DashboardTheme.SURFACE_2,
                                DashboardTheme.BORDER,
                                8,
                                1,
                                false
                        )
                );
            }
        }

        tabsPanel.repaint();
    }

    // =========================================================
    // TAB HELPERS
    // =========================================================

    private boolean isSelectedTab(
            JPanel tab
    ) {

        return tab.getBorder()
                instanceof RoundedBackgroundBorder
                && ((RoundedBackgroundBorder)
                tab.getBorder())
                .isActive();
    }

    private int getTabIndex(
            JPanel tab
    ) {

        int index = 0;

        for (
                Component component :
                tabsPanel.getComponents()
        ) {

            if (!(component instanceof JPanel)) {
                continue;
            }

            if (component == tab) {
                return index;
            }

            index++;
        }

        return -1;
    }

    private int getTabCount() {

        int count = 0;

        for (
                Component component :
                tabsPanel.getComponents()
        ) {

            if (component instanceof JPanel) {
                count++;
            }
        }

        return count;
    }

    private Component getTabComponent(
            int index
    ) {

        int current = 0;

        for (
                Component component :
                tabsPanel.getComponents()
        ) {

            if (!(component instanceof JPanel)) {
                continue;
            }

            if (current == index) {
                return component;
            }

            current++;
        }

        return null;
    }

    // =========================================================
    // LISTENERS
    // =========================================================

    private void installListeners() {

        leftCollapseButton.addActionListener(
                e -> {

                    leftSidebarCollapsed =
                            !leftSidebarCollapsed;

                    refreshLayout();
                }
        );

        rightCollapseButton.addActionListener(
                e -> {

                    rightSidebarCollapsed =
                            !rightSidebarCollapsed;

                    refreshLayout();
                }
        );
    }

    // =========================================================
    // SIDEBAR LAYOUT
    // =========================================================

    private void refreshLayout() {

        updateLeftSidebar();

        updateRightSidebar();

        mainPanel.revalidate();

        mainPanel.repaint();
    }

    // =========================================================
    // LEFT SIDEBAR
    // =========================================================

    private void updateLeftSidebar() {
        updateSidebarSize(leftSidebar, leftSidebarCollapsed, false);

        if (leftSidebarCollapsed) {
            leftTopBar.setVisible(true);
            leftHorizontalTitle.setVisible(false);
            leftSidebarTitle.setVisible(true);
            leftSidebarTitle.setRotation(RotatableLabel.Rotation.COUNTER_CLOCKWISE);
            leftCollapseButton.setText("›");

            // En mode collapsed, on ne garde que le titre vertical
            // On cache le contenu du data panel
            if (leftCollapsedContent.getComponentCount() > 1) {
                Component dataPanel = leftCollapsedContent.getComponent(1);
                if (dataPanel != null) {
                    dataPanel.setVisible(false);
                }
            }
        } else {
            leftTopBar.setVisible(true);
            leftHorizontalTitle.setVisible(true);
            leftSidebarTitle.setVisible(false);
            leftCollapseButton.setText("‹");

            // En mode étendu, on montre le data panel
            if (leftCollapsedContent.getComponentCount() > 1) {
                Component dataPanel = leftCollapsedContent.getComponent(1);
                if (dataPanel != null) {
                    dataPanel.setVisible(true);
                }
            }
        }

        leftSidebar.revalidate();
        leftSidebar.repaint();
    }

    // =========================================================
    // RIGHT SIDEBAR
    // =========================================================

    private void updateRightSidebar() {
        updateSidebarSize(rightSidebar, rightSidebarCollapsed, true);

        // Trouver le panel de visualisations
        Component vizPanel = null;
        for (Component comp : rightCollapsedContent.getComponents()) {
            if (comp instanceof JPanel && "visualizationPanelWrapper".equals(comp.getName())) {
                vizPanel = comp;
                break;
            }
        }

        if (rightSidebarCollapsed) {
            rightTopBar.setVisible(true);
            rightHorizontalTitle.setVisible(false);
            rightSidebarTitle.setVisible(true);
            rightSidebarTitle.setRotation(RotatableLabel.Rotation.COUNTER_CLOCKWISE);
            rightCollapseButton.setText("‹");

            if (vizPanel != null) {
                vizPanel.setVisible(false);
            }
        } else {
            rightTopBar.setVisible(true);
            rightHorizontalTitle.setVisible(true);
            rightSidebarTitle.setVisible(false);
            rightCollapseButton.setText("›");

            if (vizPanel != null) {
                vizPanel.setVisible(true);
            }
        }

        rightSidebar.revalidate();
        rightSidebar.repaint();
    }

    // =========================================================
    // SIDEBAR SIZE
    // =========================================================

    private void updateSidebarSize(
            JPanel sidebar,
            boolean collapsed,
            boolean isExtended
    ) {

        if (collapsed) {

            /*
             * COLLAPSED :
             *
             * largeur STRICTEMENT fixe.
             *
             * Le GridLayoutManager ne pourra donc
             * pas agrandir cette colonne.
             */
            Dimension size =
                    new Dimension(
                            DashboardTheme.COLLAPSED_WIDTH,
                            0
                    );

            sidebar.setPreferredSize(size);
            sidebar.setMinimumSize(size);
            sidebar.setMaximumSize(size);

        } else {

            /*
             * OUVERT :
             *
             * largeur flexible.
             */
            sidebar.setPreferredSize(
                    new Dimension(
                            isExtended ? DashboardTheme.EXPANDED_WIDTH_EXTEND :
                                    DashboardTheme.EXPANDED_WIDTH,
                            0
                    )
            );

            sidebar.setMinimumSize(
                    new Dimension(
                            DashboardTheme.MIN_WIDTH,
                            100
                    )
            );

            sidebar.setMaximumSize(
                    new Dimension(
                            DashboardTheme.MAX_WIDTH,
                            Integer.MAX_VALUE
                    )
            );
        }

        sidebar.setVisible(true);
    }
}