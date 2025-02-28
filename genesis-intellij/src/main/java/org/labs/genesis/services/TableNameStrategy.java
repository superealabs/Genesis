package org.labs.genesis.services;


import lombok.Getter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Database;

import java.sql.Connection;
import java.util.List;

@Getter
public abstract class TableNameStrategy {
    private final Database database;
    private final Connection connection;
    private final String selectAll;

    public TableNameStrategy(ProjectGenerationContext projectGenerationContext, String selectAll) {
        this.database = projectGenerationContext.getDatabase();
        this.connection = projectGenerationContext.getConnection();
        this.selectAll = selectAll;
    }

    protected void checkIsNotNull() {
        if (database == null || connection == null) {
            throw new IllegalStateException("Database or connection is not defined.");
        }
    }

    abstract public List<String> getTableNames() throws Exception;
}
