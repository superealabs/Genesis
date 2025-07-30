package org.labs.genesis.frontend.generator;

import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.frontend.FrontendLanguage;

public interface IFrontendGenerator {
    public  String generateComponent(FrontendLanguage language, TableMetadata tableMetadata);
    public  String generateService(FrontendLanguage language, TableMetadata tableMetadata);
    public  String generateModel(FrontendLanguage language, TableMetadata tableMetadata);
}
