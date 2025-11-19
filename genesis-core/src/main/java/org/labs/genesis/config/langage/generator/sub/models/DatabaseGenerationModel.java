package org.labs.genesis.config.langage.generator.sub.models;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Credentials;

@Getter
@Setter
public class DatabaseGenerationModel {
    private int databaseId;
    private Credentials credentials;
    public DatabaseGenerationModel(ProjectGenerationContext context) {
        this.databaseId = context.getDatabase().getId();
        this.credentials = context.getCredentials();
    }
}
