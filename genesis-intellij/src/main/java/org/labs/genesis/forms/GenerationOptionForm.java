package org.labs.genesis.forms;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.listener.MoreButtonListener;
import org.labs.genesis.services.TableNameStrategy;
import org.labs.genesis.services.tablename.TableNamePaginatorStrategy;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;


@Getter
public class GenerationOptionForm {
    public static String SELECT_ALL = "* (select all)";
    private ProjectGenerationContext projectGenerationContext;
    private JPanel mainPanel;
    private JBList<String> tableNamesList;
    private JBList<String> viewNamesList;
    private JLabel tableAvailableLabel;
    private LinkLabel<String> refreshLinkLabel;
    private JBList<String> componentChoice;
    private JLabel componentsLabel;
    private JButton nextButton;
    private JLabel viewAvailableLabel;
    private JButton nextViewButton;

    private JComboBox<String> comboBoxProjectList;
    private JComboBox<ProjectGenerationContext> contextList ;
    private JLabel labelListProject;
    private JButton addGenerationButton;
    private final List<ProjectGenerationContext> listProjectGenerationContexts ;

    @Setter
    private List<String> allTablesNames ;
    @Setter
    private List<String> allViewsNames ;

    @Setter
    private int paginationIndex = 0;
    private int paginationSize = 1;

    @Setter
    private int paginationListViewIndex = 0;

    private TableNameStrategy tableNameStrategy;
    private boolean initialized = false;

    public GenerationOptionForm(ProjectGenerationContext projectGenerationContext , List<ProjectGenerationContext> listProjectGenerationContexts) {
        this.projectGenerationContext = projectGenerationContext;
        this.listProjectGenerationContexts = listProjectGenerationContexts;
        contextList = new JComboBox<>();
        this.allTablesNames = new ArrayList<>();
        this.allViewsNames = new ArrayList<>();

        setupListeners();
        setupMoreListener();

    }
    public void selectContext() {
        if (listProjectGenerationContexts != null && !listProjectGenerationContexts.isEmpty()) {
            this.projectGenerationContext = listProjectGenerationContexts.get(0);
        }
    }

    public void refreshUI(boolean isMultiProject) {
        if (isMultiProject) {
            addListProject();
            if(!initialized) {
                initialized = true;
                selectContext();
            }
            comboBoxProjectList.setVisible(true);
            labelListProject.setVisible(true);
            addGenerationButton.setVisible(true);
        } else {
            comboBoxProjectList.setVisible(false);
            labelListProject.setVisible(false);
            addGenerationButton.setVisible(false);
        }
    }

    public void addListProject() {
        DefaultComboBoxModel<String> nameModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<ProjectGenerationContext> contextModel = new DefaultComboBoxModel<>();

        for (ProjectGenerationContext context : listProjectGenerationContexts) {
            nameModel.addElement(context.getProjectName() + " " + context.getFramework().getName() + " " +context.getDatabase().getName() + " " + context.getCredentials().getDatabaseName());
            contextModel.addElement(context);
        }
        comboBoxProjectList.setModel(nameModel);
        comboBoxProjectList.setVisible(true);
        comboBoxProjectList.revalidate();
        comboBoxProjectList.repaint();

        contextList.setModel(contextModel);
        contextList.setVisible(true);
        contextList.revalidate();
        contextList.repaint();

        comboBoxProjectList.addActionListener(e -> {
            int index = comboBoxProjectList.getSelectedIndex();
            if (index >= 0 && index < contextList.getModel().getSize()) {
                contextList.setSelectedIndex(index);
            }
        });

    }

    private void refreshTableAndViewForSelectedContext() {
        if (projectGenerationContext == null) return;

        paginationIndex = 0;
        paginationListViewIndex = 0;

        allTablesNames.clear();
        allViewsNames.clear();

        populateTableNames();
        populateViewNames();

    }

    private void setupMoreListener() {
        nextButton.addMouseListener(new MoreButtonListener(this));
        nextViewButton.addMouseListener(new MoreButtonListener(this));
    }


    private void setupListeners() {
        assert refreshLinkLabel != null;

//        refreshLinkLabel.setListener((LinkLabel<String> source, String data) -> populateTableNames(), null);
//
//        refreshLinkLabel.setListener((LinkLabel<String> source, String data) -> populateViewNames(), null);
        refreshLinkLabel.setListener((src, data) -> {
            int selectedIndex = comboBoxProjectList.getSelectedIndex(); // index visible dans la combo
            if (selectedIndex >= 0 && selectedIndex < listProjectGenerationContexts.size()) {
                this.projectGenerationContext = listProjectGenerationContexts.get(selectedIndex);
                refreshTableAndViewForSelectedContext();
            }else {
                populateTableNames();
                populateViewNames();
            }
        }, null);


        refreshLinkLabel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                handleLinkLabelShown();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                handleLinkLabelShown();
            }
        });
    }

    private void handleLinkLabelShown() {
        Connection conn = projectGenerationContext.getConnection();
        if (conn == null) {
            System.out.println("Connection not ready yet, skipping populateTableNames");
            return; // Ne pas appeler populateTableNames() si la connection n'est pas prête
        }

        populateTableNames();
        populateViewNames();
        refreshLinkLabel.setFocusable(true);
        refreshLinkLabel.setToolTipText("Click to refresh table names");
    }

    private void populateTableNames() {
        String message = "before tableNamePaginatorStrategy";
        try {
            this.tableNameStrategy = new TableNamePaginatorStrategy(this.projectGenerationContext, SELECT_ALL, this);
            message = "before tableNameStrategy";
            List<String> list = tableNameStrategy.getTableNames();
            message = "after tableNameStrategy";
            this.allTablesNames.addAll(list);
            tableNamesList.setListData(this.allTablesNames.toArray(new String[0]));
        } catch (IllegalStateException e) {
            Messages.showErrorDialog(
                    mainPanel,
                    e.getMessage(),
                    "Error"
            );
        } catch (Exception e) {
            // e.printStackTrace();
            Messages.showErrorDialog(
                    mainPanel,
                    "Failed to retrieve table names: " + e.getMessage() + ", message personnalized : " + message,
                    "Error"
            );
        }
    }

    private void populateViewNames() {
        try {
            this.tableNameStrategy = new TableNamePaginatorStrategy(this.projectGenerationContext, SELECT_ALL, this);
            List<String> list = tableNameStrategy.getViewNames();
            this.allViewsNames.addAll(list);
            viewNamesList.setListData(this.allViewsNames.toArray(new String[0]));
        } catch (IllegalStateException e) {
            Messages.showErrorDialog(
                    mainPanel,
                    e.getMessage(),
                    "Error"
            );
        } catch (Exception e) {
            Messages.showErrorDialog(
                    mainPanel,
                    "Failed to retrieve view names: " + e.getMessage(),
                    "Error"
            );
        }
    }

    public List<String> getAllTableNames() throws Exception {
        Database database = projectGenerationContext.getDatabase();
        Connection connection = projectGenerationContext.getConnection();

        if (database == null || connection == null) {
            throw new IllegalStateException("Database or connection is not defined.");
        }

        // Récupérer les noms de tables et ajouter l'option spéciale
        List<String> allTableNames = database.getAllTableNames(connection);
        allTableNames.addFirst(SELECT_ALL); // Ajouter l'option pour tout sélectionner
        return allTableNames;
    }
    public List<String> getAllTableNames(ProjectGenerationContext projectGenerationContext) throws Exception {
        Database database = projectGenerationContext.getDatabase();
        Connection connection = projectGenerationContext.getConnection();

        if (database == null || connection == null) {
            throw new IllegalStateException("Database or connection is not defined.");
        }

        // Récupérer les noms de tables et ajouter l'option spéciale
        List<String> allTableNames = database.getAllTableNames(connection);
        allTableNames.addFirst(SELECT_ALL); // Ajouter l'option pour tout sélectionner
        return allTableNames;
    }

    private void connectionIsValid(Connection connection) throws Exception {

        if (!connection.isValid(2)) {
            throw new IllegalStateException("Database connection is not valid.");
        } else {
            System.out.println("Connection to the database is established." + connection.getMetaData().getURL());
        }
    }

    public void incrementTableNameList() {
        this.populateTableNames();
    }
}

