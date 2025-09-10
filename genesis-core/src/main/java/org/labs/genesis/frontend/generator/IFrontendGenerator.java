package org.labs.genesis.frontend.generator;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.FrontendLanguage;

public interface IFrontendGenerator {
    public  String generateComponent(Database database,FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String generateService(Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String generateModel(Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String generateRessources(ProjectGenerationContext context)throws  Exception;
}
