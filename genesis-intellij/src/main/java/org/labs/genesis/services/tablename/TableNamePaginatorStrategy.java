package org.labs.genesis.services.tablename;

import org.labs.genesis.forms.GenerationOptionForm;
import org.labs.genesis.services.TableNameStrategy;
import org.labs.genesis.config.ProjectGenerationContext;

import java.util.List;

public class TableNamePaginatorStrategy extends TableNameStrategy {
    private final int NB_TABLE = 5;
    private final GenerationOptionForm generationOptionForm;
    public TableNamePaginatorStrategy(ProjectGenerationContext projectGenerationContext, String selectAll, GenerationOptionForm generationOptionForm) {
        super(projectGenerationContext, selectAll);
        this.generationOptionForm = generationOptionForm;
    }

    @Override
    public List<String> getTableNames() throws Exception {
        this.checkIsNotNull();
        // Récupérer les noms de tables et ajouter l'option spéciale
        if (this.generationOptionForm.getAllTablesNames() == null) {
            List<String> allTableNames = this.getDatabase().getAllTableNames(this.getConnection());
            allTableNames.addFirst(this.getSelectAll()); // Ajouter l'option pour tout sélectionner "Message"
            this.generationOptionForm.setAllTablesNames(allTableNames);
        }
        int firstIndex = this.NB_TABLE * this.generationOptionForm.getPaginationIndex();
        int lastIndex = Math.min(firstIndex + this.NB_TABLE, this.generationOptionForm.getAllTablesNames().size());
        return this.generationOptionForm.getAllTablesNames().subList(firstIndex, lastIndex);
    }
}
