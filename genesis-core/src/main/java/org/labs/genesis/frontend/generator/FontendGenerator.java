package org.labs.genesis.frontend.generator;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.genesis.frontend.generator.model.*;
import org.labs.utils.FileUtils;
import org.labs.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;

public class FontendGenerator implements IFrontendGenerator{
    private final GenesisTemplateEngine engine;

    public FontendGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public String generateComponent(String securityType, Database database, FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception {
        if(language.getId()!=frontendFramework.getLanguageId()){
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the frontend framework '" +
                    frontendFramework.getName() + "' (required language ID: '" + frontendFramework.getLanguageId() + "').");
        }
        String templateArchitecture = loadTemplate(frontendFramework);
        HashMap<String, Object> metadataForFinalRender = FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, destinationFolder, projectName);
        metadataForFinalRender.putAll(FrameworkFrontendMetadataProvider.getHashMapForSecurity(securityType));

        for(Component component:frontendFramework.getComponents()) {
            if(tableMetadata.getIsView() && !component.getGenerateForView())
            {
                continue;
            }
            HashMap<String, Object> metadataPrimary = FrameworkFrontendMetadataProvider.getComponentHashMap(component, language, tableMetadata);
            String structure = engine.simpleRender(templateArchitecture, metadataPrimary);

            String finalStringForComponent = engine.render(structure,metadataForFinalRender );
            finalStringForComponent=engine.simpleRenderAlt(finalStringForComponent,metadataForFinalRender);

            String fileSavePath;
            if (generateComponentOnly) {
                // simplified path : destinationFolder/projectName/models
                fileSavePath = destinationFolder + "/" + projectName + "/models";
            } else {
                //using the configured path in the frontendframework
                fileSavePath = component.getDestinationPath();
                fileSavePath = engine.simpleRender(fileSavePath, metadataForFinalRender);
            }
            // ensure that the folder exists
            FileUtils.createDirectory(fileSavePath);

            String componentName=component.getComponentName();
            componentName=engine.render(componentName, metadataForFinalRender);

            // creating matching file
            String fileName = componentName;
            FileUtils.createFile(fileSavePath, fileName, frontendFramework.getComponentExtension(), finalStringForComponent);

            ProjectGenerator.renderFilesEdits(component.getComponentAdditionalFiles(), metadataForFinalRender);

            component.setEntityName(tableMetadata.getTableName());
            // get the route for the component
            if (component.getRouter().getLink() != null && !component.getRouter().getLink().isEmpty()) {
                ComponentRoute router = new ComponentRoute();
                
                String componentSelector = ProjectGenerator.engine.simpleRender(component.getSelector(),metadataForFinalRender);
                if ( component.getRouter().getLabel() != null && !component.getRouter().getLabel().isEmpty()) {
                    String componentRouterLabel = ProjectGenerator.engine.simpleRender(component.getRouter().getLabel(), metadataForFinalRender);
                    router.setLabel(componentRouterLabel);
                }
                String componentRouterLink = ProjectGenerator.engine.simpleRender(component.getRouter().getLink(),metadataForFinalRender);
                String componentImportPath = StringUtils.replaceUntilMarker(fileSavePath, "src/", "@/");
                String componentImportWithoutExtensionPath = componentImportPath+ "/"+componentName;
                componentImportPath += "/"+componentName+"."+frontendFramework.getComponentExtension();

                router.setLink(componentRouterLink);
                router.setComponentName(componentName);
                router.setComponentSelector(componentSelector);
                router.setComponentImport(componentImportPath);
                router.setComponentImportWithoutExtension(componentImportWithoutExtensionPath);
                router.setEntityName(tableMetadata.getClassName());
                frontendFramework.addRoute(router);
            }

        }
        return "";
    }

    @Override
    public String generateService(Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly)throws Exception {
        if(language.getId()!=frontendFramework.getLanguageId()){
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the frontend framework '" +
                    frontendFramework.getName() + "' (required language ID: '" + frontendFramework.getLanguageId() + "').");
        }

        ServiceComponent serviceComponent= frontendFramework.getServiceComponent();

        String templateArchitecture = loadTemplateForServices(frontendFramework);
        HashMap<String,Object> metadataPrimary = FrameworkFrontendMetadataProvider.getServiceHashMap(serviceComponent, language, tableMetadata);
        String structure = engine.simpleRender(templateArchitecture, metadataPrimary);

        HashMap<String,Object> metadataForFinalRender= FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, destinationFolder, projectName);
        String finalStringForService = engine.render(structure,metadataForFinalRender);
        finalStringForService=engine.simpleRenderAlt(finalStringForService,metadataForFinalRender);


        String fileSavePath;
        if (generateComponentOnly) {
            // simplified path : destinationFolder/projectName/models
            fileSavePath = destinationFolder + "/" + projectName + "/models";
        } else {
            //using the configured path in the frontendframework
            fileSavePath = serviceComponent.getDestinationPath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataForFinalRender);
        }


        String serviceName=serviceComponent.getName();
        serviceName=engine.render(serviceName, metadataForFinalRender);

        // Creating matching file
        String fileName = serviceName;
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), finalStringForService);

        return "";
    }

    @Override
    public String generateModel(Database database,FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly) throws Exception{

        if(language.getId()!=frontendFramework.getLanguageId()){
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the frontend framework '" +
                    frontendFramework.getName() + "' (required language ID: '" + frontendFramework.getLanguageId() + "').");
        }

        ModelComponent modelComponent=frontendFramework.getModelComponent();

        String structure=loadTemplateForModel(frontendFramework);

        HashMap<String, Object> metadataPrimary = FrameworkFrontendMetadataProvider.getModelHashMap(modelComponent, language, tableMetadata);
        structure = engine.simpleRender(structure,metadataPrimary);
        HashMap<String, Object> metadataForFinalRender = FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, destinationFolder, projectName);
        String finalStringForModel = engine.render(structure,metadataForFinalRender);
        finalStringForModel=engine.simpleRenderAlt(finalStringForModel,metadataForFinalRender);

        String fileSavePath;
        if (generateComponentOnly) {
            // simplified path : destinationFolder/projectName/models
            fileSavePath = destinationFolder + "/" + projectName + "/models";
        } else {
            //using the configured path in the frontendframework
            fileSavePath = modelComponent.getDestinationPath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataForFinalRender);
        }
        // ensure that the folder exists

        String modelName=modelComponent.getName();
        modelName=engine.render(modelName, metadataForFinalRender);

        FileUtils.createDirectory(fileSavePath);

        String fileName = modelName;

        FileUtils.createFile(fileSavePath,fileName, language.getExtension(), finalStringForModel);

        return "";
    }

    @Override
    public  String generateRessources(ProjectGenerationContext context, List<TableMetadata> allEntities) throws  Exception{
        FrontendFramework frontendFramework = context.getFrontendFramework();
        HashMap<String, Object> metadata = FrameworkFrontendMetadataProvider.getLangsHashMap(context, allEntities);
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
        // Generate lang files
        List<InterfaceLang> langs = frontendFramework.getFrontendLayout().getLangs();
        String langPath = FrontendDestinationPaths.normalizePath(engine.simpleRender(frontendFramework.getFrontendPaths().langsPath, metadata));
        for (InterfaceLang lang : langs) {
            String content = engine.render(lang.getContent(),metadata);
            FileUtils.createFile(langPath,lang.getName().toLowerCase(), context.getFrontendLanguage().getExtension(), content);
        }
        return "";
    }


    private String loadTemplate(FrontendFramework frontendFramework) throws IOException {
        return FileUtils.getFileContent(Constantes.FRONTEND_TEMPLATE_DIRECTORY+"/"+ frontendFramework.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
    }

    private String loadTemplateForServices(FrontendFramework frontendFramework) throws IOException {
        return FileUtils.getFileContent(Constantes.FRONTEND_TEMPLATE_DIRECTORY + "/" + frontendFramework.getTemplate() + "-service." + Constantes.MODEL_TEMPLATE_EXT);
    }

    private String loadTemplateForModel(FrontendFramework frontendFramework) throws IOException {
        return FileUtils.getFileContent(Constantes.FRONTEND_TEMPLATE_DIRECTORY + "/" + frontendFramework.getTemplate() + "-model." + Constantes.MODEL_TEMPLATE_EXT);
    }
}
