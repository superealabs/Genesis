package org.labs.genesis.services.tablename;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.services.TableNameStrategy;

import java.util.List;

public class TableNameStrategyImpl extends TableNameStrategy {


    public TableNameStrategyImpl(ProjectGenerationContext projectGenerationContext, String selectAll) {
        super(projectGenerationContext, selectAll);
    }

    @Override
    public List<String> getTableNames() throws Exception {
        this.checkIsNotNull();
        // Récupérer les noms de tables et ajouter l'option spéciale
        List<String> allTableNames = this.getDatabase().getAllTableNames(this.getConnection());
        allTableNames.addFirst(this.getSelectAll()); // Ajouter l'option pour tout sélectionner
        return allTableNames;
    }
}
