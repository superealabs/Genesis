package org.labs.genesis.frontend.generator.resources;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.genesis.frontend.generator.model.FrontendDestinationPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

public class BrandingGenerator implements IResourceGenerator{
    private final GenesisTemplateEngine engine;

    public BrandingGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public String generateRessources(ProjectGenerationContext context, HashMap<String, Object> metadata) throws Exception {
        FrontendFramework frontendFramework = context.getFrontendFramework();
        if (frontendFramework == null) {
            return "";
        }
        // Generate logo
        String logoPath = frontendFramework.getFrontendPaths().getLogoPath();
        logoPath = FrontendDestinationPaths.normalizePath(engine.simpleRender(logoPath, metadata));
        if (!frontendFramework.getProjectBranding().useLogoLink() && frontendFramework.getProjectBranding().hasLogo()){
            File logoFile = frontendFramework.getProjectBranding().getLogoFile();
            try{
                Path targetPath = Paths.get(logoPath, frontendFramework.getProjectBranding().getLogoUrl());
                Files.createDirectories(targetPath.getParent());
                Files.copy(logoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e){
                throw new Exception("Unable to upload the logo file at "+logoPath+" : "+e.getMessage(),e);
            }
        }

        // Generate favicon
        String faviconPath = frontendFramework.getFrontendPaths().getFaviconPath();
        faviconPath = FrontendDestinationPaths.normalizePath(engine.simpleRender(faviconPath, metadata));
        if (!frontendFramework.getProjectBranding().useFaviconLink() && frontendFramework.getProjectBranding().hasFavicon()){
            File faviconFile = frontendFramework.getProjectBranding().getFaviconFile();
            try{
                Path targetPath = Paths.get(faviconPath,frontendFramework.getProjectBranding().getFaviconUrl());
                Files.copy(faviconFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e){
                throw new Exception("Unable to updload the favicon at "+faviconPath+" : "+e.getMessage(),e);
            }
        }

        return "Branding resources generated.";
    }
}
