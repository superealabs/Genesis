package org.labs.genesis.frontend.generator;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.form.FieldConfiguration;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.FrontendLanguage;

import java.util.List;

public interface IFrontendGenerator {
    public  String generateComponent(String securityType, Database database,FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String generateComponent(String securityType, Database database,FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly, java.util.Map<String, java.util.Map<String, FieldConfiguration>> formConfiguration)throws Exception;
    public  String generateService(String securityType,Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String generateService(String securityType,Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly, java.util.Map<String, java.util.Map<String, FieldConfiguration>> formConfiguration)throws Exception;
    public  String generateModel(String securityType,Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String generateModel(String securityType,Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly, java.util.Map<String, java.util.Map<String, FieldConfiguration>> formConfiguration)throws Exception;
    public  String generateRessources(ProjectGenerationContext context, List<TableMetadata> allEntities)throws  Exception;
}
