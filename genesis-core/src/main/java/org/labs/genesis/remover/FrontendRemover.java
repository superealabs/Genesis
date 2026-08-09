package org.labs.genesis.remover;

import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.genesis.frontend.generator.model.Component;
import org.labs.genesis.frontend.generator.model.ModelComponent;
import org.labs.genesis.frontend.generator.model.ServiceComponent;
import org.labs.utils.FileUtils;

import java.util.HashMap;

public class FrontendRemover implements IFrontendRemover{
    private final GenesisTemplateEngine engine;

    public FrontendRemover(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public String removeComponent(FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly) throws Exception {
        if(language.getId()!=frontendFramework.getLanguageId()){
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the frontend framework '" +
                    frontendFramework.getName() + "' (required language ID: '" + frontendFramework.getLanguageId() + "').");
        }
        HashMap<String, Object> metadataForFinalRender = FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, destinationFolder, projectName);
        for(Component component:frontendFramework.getComponents()){
            if(tableMetadata.getIsView() && !component.getGenerateForView())
            {
                continue;
            }
            String fileSavePath;
            fileSavePath = component.getDestinationPath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataForFinalRender);
            String componentName=component.getComponentName();
            componentName=engine.render(componentName, metadataForFinalRender);
            FileUtils.deleteFile(fileSavePath, componentName, frontendFramework.getComponentExtension());
        }
        return "";
    }

    @Override
    public String removeService(FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly) throws Exception {
        if(language.getId()!=frontendFramework.getLanguageId()){
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the frontend framework '" +
                    frontendFramework.getName() + "' (required language ID: '" + frontendFramework.getLanguageId() + "').");
        }
        ServiceComponent serviceComponent=frontendFramework.getServiceComponent();
        HashMap<String,Object> metadataForFinalRender= FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, destinationFolder, projectName);
        String fileSavePath;
        fileSavePath = serviceComponent.getDestinationPath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataForFinalRender);
        String serviceName=serviceComponent.getName();
        serviceName=engine.render(serviceName, metadataForFinalRender);
        String fileName = serviceName;
        FileUtils.deleteFile(fileSavePath,fileName, language.getExtension());
        return "";
    }

    @Override
    public String removeModel(FrontendLanguage language, FrontendFramework frontendFramework, TableMetadata tableMetadata, String destinationFolder, String projectName, boolean generateComponentOnly) throws Exception {
        if(language.getId()!=frontendFramework.getLanguageId()){
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the frontend framework '" +
                    frontendFramework.getName() + "' (required language ID: '" + frontendFramework.getLanguageId() + "').");
        }
        ModelComponent modelComponent=frontendFramework.getModelComponent();
        HashMap<String, Object> metadataForFinalRender = FrameworkFrontendMetadataProvider.getHashMapIntermediaire(tableMetadata, destinationFolder, projectName);
        String fileSavePath;
        fileSavePath = modelComponent.getDestinationPath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataForFinalRender);
        String modelName=modelComponent.getName();
        modelName=engine.render(modelName, metadataForFinalRender);
        FileUtils.createDirectory(fileSavePath);
        String fileName = modelName;
        FileUtils.deleteFile(fileSavePath,fileName, language.getExtension());
        return "";
    }
}
