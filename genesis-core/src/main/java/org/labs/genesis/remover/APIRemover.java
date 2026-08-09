package org.labs.genesis.remover;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.util.HashMap;
import java.util.Map;

import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.*;
import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.getPrimaryModelDaoHashMap;

public class APIRemover implements IAPIRemover {
    private final GenesisTemplateEngine engine;

    public APIRemover(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    @Override
    public String removeModel(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/models";
        } else {
            fileSavePath = framework.getModel().getModelSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }
        String fileName = tableMetadata.getClassName();
        FileUtils.deleteFile(fileSavePath, fileName, language.getExtension());
        ProjectGenerator.removeFilesEdits(framework.getModel().getModelAdditionalFiles(), metadataFinally);
        return fileSavePath;
    }

    @Override
    public String removeDao(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);
        metadataFinally.putAll(getPrimaryModelDaoHashMap(framework, tableMetadata));

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/repositories";
        } else {
            if (framework.getModelDao() == null) {
                throw new RuntimeException("ModelDao is not configured for framework: " + framework.getName());
            }
            fileSavePath = framework.getModelDao().getModelDaoSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }
        String fileName;
        if (generateComponentOnly) {
            fileName = tableMetadata.getClassName() + "Repository";
        } else {
            if (framework.getModelDao() == null) {
                throw new RuntimeException("ModelDao is not configured for framework: " + framework.getName());
            }
            fileName = framework.getModelDao().getModelDaoName();
            fileName = engine.simpleRender(fileName, metadataFinally);
        }
        if(framework.getId()==Constantes.ExpressJs_ID){
            String className = tableMetadata.getClassName();
            fileName = className.substring(0,1).toLowerCase() + className.substring(1)+".entity";
        }
        FileUtils.deleteFile(fileSavePath, fileName, language.getExtension());
        ProjectGenerator.removeFilesEdits(framework.getModelDao().getModelDaoAdditionalFiles(), metadataFinally);
        return fileSavePath;
    }

    @Override
    public String removeService(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/services";
        } else {
            fileSavePath = framework.getService().getServiceSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }
        String fileName;
        if (generateComponentOnly) {
            // convention de nommage node
            if(framework.getId()== Constantes.ExpressJs_ID){
                String className = tableMetadata.getClassName();
                fileName = className.substring(0,1).toLowerCase() + className.substring(1) + ".service";
            }else {
                fileName = tableMetadata.getClassName() + "Service";
            }
        } else {
            System.out.println ("GENERATE SERVICE FRAMEWORK = "+ framework.getId());
            if(framework.getId()== Constantes.ExpressJs_ID){
                String className = tableMetadata.getClassName();
                fileName = className.substring(0,1).toLowerCase() + className.substring(1) + ".service";
                fileName = engine.simpleRender(fileName, metadataFinally);
            }else {
                fileName = framework.getService().getServiceName();
                fileName = engine.simpleRender(fileName, metadataFinally);
            }
        }
        FileUtils.deleteFile(fileSavePath, fileName, language.getExtension());
        ProjectGenerator.removeFilesEdits(framework.getService().getServiceAdditionalFiles(), metadataFinally);
        return fileSavePath;
    }

    @Override
    public String removeController(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/controllers";
        } else {
            fileSavePath = framework.getController().getControllerSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }
        String fileName;
        if (generateComponentOnly) {
            // convention de nommage node
            if(framework.getId()== Constantes.ExpressJs_ID){
                String className = tableMetadata.getClassName();
                fileName = className.substring(0,1).toLowerCase() + className.substring(1) + ".controller";
            }else {
                fileName = tableMetadata.getClassName() + "Controller";
            }
        } else {
            if(framework.getId()== Constantes.ExpressJs_ID){
                String className = tableMetadata.getClassName();
                fileName = className.substring(0,1).toLowerCase() + className.substring(1) + ".controller";
                fileName = engine.simpleRender(fileName, metadataFinally);
            }else {

                fileName = framework.getController().getControllerName();
                fileName = engine.simpleRender(fileName, metadataFinally);
            }
        }
        FileUtils.deleteFile(fileSavePath, fileName, language.getExtension());
        ProjectGenerator.removeFilesEdits(framework.getController().getControllerAdditionalFiles(), metadataFinally);
        return fileSavePath;
    }

}
