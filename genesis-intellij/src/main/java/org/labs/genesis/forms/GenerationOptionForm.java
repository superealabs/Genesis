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
import java.sql.Connection;
import java.util.List;

@Getter
public class GenerationOptionForm {
    public static String SELECT_ALL = "* (select all)";
    private final ProjectGenerationContext projectGenerationContext;
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
    @Setter
    private List<String> allTablesNames = null;
    @Setter
    private List<String> allViewsNames = null;

    @Setter
    private int paginationIndex = 0;
    private int paginationSize = 1;

    @Setter
    private int paginationListViewIndex = 0;

    private TableNameStrategy tableNameStrategy;

    public GenerationOptionForm(ProjectGenerationContext projectGenerationContext) {
        this.projectGenerationContext = projectGenerationContext;
        setupListeners();
        setupMoreListener();

    }

    private void setupMoreListener() {
        nextButton.addMouseListener(new MoreButtonListener(this));
        nextViewButton.addMouseListener(new MoreButtonListener(this));
    }


    private void setupListeners() {
        assert refreshLinkLabel != null;

        refreshLinkLabel.setListener((LinkLabel<String> source, String data) -> populateTableNames(), null);

        refreshLinkLabel.setListener((LinkLabel<String> source, String data) -> populateViewNames(), null);

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
        populateTableNames();
        populateViewNames();
        refreshLinkLabel.setFocusable(true);
        refreshLinkLabel.setToolTipText("Click to refresh table names");
    }

    private void populateTableNames() {
        try {
            this.tableNameStrategy = new TableNamePaginatorStrategy(this.projectGenerationContext, SELECT_ALL, this);
            List<String> list = tableNameStrategy.getTableNames();
            this.allTablesNames.addAll(list);
            tableNamesList.setListData(this.allTablesNames.toArray(new String[0]));
        } catch (IllegalStateException e) {
            Messages.showErrorDialog(
                    mainPanel,
                    e.getMessage(),
                    "Error"
            );
        } catch (Exception e) {
            Messages.showErrorDialog(
                    mainPanel,
                    "Failed to retrieve table names: " + e.getMessage(),
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

