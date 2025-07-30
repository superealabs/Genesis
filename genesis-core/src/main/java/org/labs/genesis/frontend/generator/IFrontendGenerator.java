package org.labs.genesis.frontend.generator;

import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.FrontendLanguage;

public interface IFrontendGenerator {
    public  String generateComponent(FrontendLanguage language, FrontendFramework frontendFramework,TableMetadata tableMetadata)throws Exception;
    public  String generateService(FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata)throws Exception;
    public  String generateModel(FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata)throws Exception;
}
