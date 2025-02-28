package org.labs.genesis.forms;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.listener.NextButtonListener;
import org.labs.genesis.listener.PreviousButtonListener;
import org.labs.genesis.services.TableNameStrategy;
import org.labs.genesis.services.tablename.TableNamePaginatorStrategy;
import org.labs.genesis.services.tablename.TableNameStrategyImpl;

import javax.swing.*;
import java.util.List;

@Getter
public class GenerationOptionForm {
    public static String SELECT_ALL = "* (select all)";
    private final ProjectGenerationContext projectGenerationContext;
    private JPanel mainPanel;
    private JBList<String> tableNamesList;
    private JLabel tableAvailableLabel;
    private LinkLabel<String> refreshLinkLabel;
    private JBList<String> componentChoice;
    private JLabel componentsLabel;
    private JButton nextButton;
    private JButton previousButton;
    private TableNameStrategy tableNameStrategy;
    @Setter
    private List<String> allTablesNames = null;
    @Setter
    private int paginationIndex = 0;

    public GenerationOptionForm(ProjectGenerationContext projectGenerationContext) {
        this.projectGenerationContext = projectGenerationContext;
        setupListeners();
        setupNextListener();
        setupPreviousListener();
    }

    private void setupNextListener() {
        nextButton.addMouseListener(new NextButtonListener(this));
    }

    private void setupPreviousListener() {
        nextButton.addMouseListener(new PreviousButtonListener(this));
    }

    private void setupListeners() {
        assert refreshLinkLabel != null;
        refreshLinkLabel.setListener((LinkLabel<String> source, String data) -> populateTableNames(), null);
    }

    public void populateTableNames() {
        try {
            //this.tableNameStrategy = new TableNamePaginatorStrategy(projectGenerationContext, SELECT_ALL, this);
            this.tableNameStrategy = new TableNameStrategyImpl(projectGenerationContext, SELECT_ALL);
            List<String> allTableNames = tableNameStrategy.getTableNames();
            tableNamesList.setListData(allTableNames.toArray(new String[0]));
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
            System.out.println("Connection to the database is established."+connection.getMetaData().getURL());
        }
    }
}

