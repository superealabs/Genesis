package org.labs.genesis.frontend.generator;

import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;

public class FontendGenerator implements IFrontendGenerator{
    private final GenesisTemplateEngine engine;

    public FontendGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public String generateComponent(FrontendLanguage language, TableMetadata tableMetadata) {
        return "";
    }

    @Override
    public String generateService(FrontendLanguage language, TableMetadata tableMetadata) {
        return "";
    }

    @Override
    public String generateModel(FrontendLanguage language, TableMetadata tableMetadata) {
        return "";
    }
}
