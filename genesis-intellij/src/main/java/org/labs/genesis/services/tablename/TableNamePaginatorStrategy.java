package org.labs.genesis.services.tablename;

import org.labs.genesis.forms.GenerationOptionForm;
import org.labs.genesis.services.TableNameStrategy;
import org.labs.genesis.config.ProjectGenerationContext;

import java.util.ArrayList;
import java.util.List;

public class TableNamePaginatorStrategy extends TableNameStrategy {
    private final int NB_TABLE = 2;
    private final GenerationOptionForm generationOptionForm;
    public TableNamePaginatorStrategy(ProjectGenerationContext projectGenerationContext, String selectAll, GenerationOptionForm generationOptionForm) {
        super(projectGenerationContext, selectAll);
        this.generationOptionForm = generationOptionForm;

        if (this.generationOptionForm.getAllTablesNames() == null || this.generationOptionForm.getAllTablesNames().isEmpty()) {
            this.generationOptionForm.setAllTablesNames(new ArrayList<>());
        }

    }

    @Override
    public List<String> getTableNames() throws Exception {
        this.checkIsNotNull();
        List<String> allTableNames = this.getDatabase().getPaginatedTableNames(this.getConnection(),this.generationOptionForm.getPaginationIndex(),this.NB_TABLE);
        if (this.generationOptionForm.getAllTablesNames().isEmpty()) {
            allTableNames.addFirst(this.getSelectAll()); // Ajouter l'option pour tout sélectionner "Message"
        }
        this.generationOptionForm.setPaginationIndex(this.generationOptionForm.getPaginationIndex() + 1);
        // this.generationOptionForm.setAllTablesNames(allTableNames);
        /*int firstIndex = this.NB_TABLE * this.generationOptionForm.getPaginationIndex();
        int lastIndex = Math.min(firstIndex + this.NB_TABLE, this.generationOptionForm.getAllTablesNames().size());
        return this.generationOptionForm.getAllTablesNames().subList(firstIndex, lastIndex);*/
        return allTableNames;
    }
}
