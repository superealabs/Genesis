package org.labs.genesis.frontend.generator.resources;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.genesis.frontend.generator.model.FrontendDestinationPaths;
import org.labs.genesis.frontend.generator.model.InterfaceLang;
import org.labs.utils.FileUtils;

import java.util.HashMap;
import java.util.List;

public class LangGenerator implements IResourceGenerator {
    private final GenesisTemplateEngine engine;

    public LangGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }
    @Override
    public String generateRessources(ProjectGenerationContext context, HashMap<String, Object> metadata) throws Exception {
        FrontendFramework frontendFramework = context.getFrontendFramework();
        if (frontendFramework == null) {
            return "";
        }
        // Generate lang files
        List<InterfaceLang> langs = frontendFramework.getFrontendLayout().getLangs();
        String langPath = FrontendDestinationPaths.normalizePath(engine.simpleRender(frontendFramework.getFrontendPaths().langsPath, metadata));
        for (InterfaceLang lang : langs) {
            String content = engine.render(lang.getContent(),metadata);
            FileUtils.createOrMergeFile(context.getDestinationFolder(), langPath,lang.getName().toLowerCase(), context.getFrontendLanguage().getExtension(), content);
        }
        return "";
    }
}
