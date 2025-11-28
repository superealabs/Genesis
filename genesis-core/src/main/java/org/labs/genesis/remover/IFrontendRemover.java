package org.labs.genesis.remover;

import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;

public interface IFrontendRemover {
    public  String removeComponent(FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String removeService(FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
    public  String removeModel(FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception;
}
