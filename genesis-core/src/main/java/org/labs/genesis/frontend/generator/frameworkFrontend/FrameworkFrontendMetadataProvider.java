package org.labs.genesis.frontend.generator.frameworkFrontend;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;

import java.util.HashMap;

public class FrameworkFrontendMetadataProvider {
    private static final GenesisTemplateEngine engine = new GenesisTemplateEngine();


    public static HashMap<String,Object> getComponentHashMap(FrontendFramework frontendFramework, FrontendLanguage frontendLanguage, TableMetadata tableMetadata)
    {
        HashMap<String, Object> metadata = new HashMap<>();

        return metadata;
    }
}
