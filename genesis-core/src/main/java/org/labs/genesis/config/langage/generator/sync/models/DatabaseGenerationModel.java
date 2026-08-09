package org.labs.genesis.config.langage.generator.sync.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;

@Getter
@Setter
@NoArgsConstructor
public class DatabaseGenerationModel {
    private int databaseId;
    private Credentials credentials;
    public DatabaseGenerationModel(ProjectGenerationContext context) {
        this.databaseId = context.getDatabase().getId();
        this.credentials = context.getCredentials();
    }

    public void addToContext(ProjectGenerationContext context) {
        context.setDatabase(ProjectGenerator.findDatabaseById(getDatabaseId()));
        context.setCredentials(getCredentials());
    }
}
