package org.labs.genesis.forms;

import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.labels.LinkLabel;
import lombok.Getter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Database;

import javax.swing.*;
import java.sql.Connection;
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

    public GenerationOptionForm(ProjectGenerationContext projectGenerationContext) {
        this.projectGenerationContext = projectGenerationContext;
        setupListeners();
    }

    private void setupListeners() {
        assert refreshLinkLabel != null;
        refreshLinkLabel.setListener((LinkLabel<String> source, String data) -> populateTableNames(), null);
    }

    public void populateTableNames() {
        try {
            List<String> allTableNames = getAllTableNames();
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

}

