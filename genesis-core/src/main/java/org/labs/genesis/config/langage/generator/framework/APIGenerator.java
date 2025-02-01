package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.HashMap;

import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.*;

public class APIGenerator implements GenesisGenerator {
    private final GenesisTemplateEngine engine;

    public APIGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    public APIGenerator() {
        this.engine = new GenesisTemplateEngine();
    }


    private String loadTemplate(Framework framework) throws IOException {
        return FileUtils.getFileContent(Constantes.DATA_PATH + "/" + framework.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
    }

    @Override
    public String generateModel(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getModelHashMap(framework, language, tableMetadata);
        String result = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);

        // Ajustement du chemin de sauvegarde en fonction de generateComponentOnly
        String fileSavePath;
        if (generateComponentOnly) {
            // Chemin simplifié : destinationFolder/projectName/models
            fileSavePath = destinationFolder + "/" + projectName + "/models";
        } else {
            // Utiliser le chemin configuré dans le framework
            fileSavePath = framework.getModel().getModelSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier correspondant
        String fileName = tableMetadata.getClassName();
        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        return result;
    }

    @Override
    public String generateDao(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getModelDaoHashMap(framework, language, tableMetadata);
        String result = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);
        metadataFinally.putAll(getPrimaryModelDaoHashMap(framework, tableMetadata));

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/repositories";
        } else {
            fileSavePath = framework.getModelDao().getModelDaoSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName;
        if (generateComponentOnly) {
            fileName = tableMetadata.getClassName() + "Repository";
        } else {
            fileName = framework.getModelDao().getModelDaoName();
            fileName = engine.simpleRender(fileName, metadataFinally);
        }

        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        // Si generateComponentOnly est false, on rend les fichiers additionnels
        if (!generateComponentOnly) {
            ProjectGenerator.renderFilesEdits(framework.getModelDao().getModelDaoAdditionalFiles(), metadataFinally);
        }

        return result;
    }

    @Override
    public String generateService(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getServiceHashMap(framework, language, tableMetadata);
        String result = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/services";
        } else {
            fileSavePath = framework.getService().getServiceSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName;
        if (generateComponentOnly) {
            fileName = tableMetadata.getClassName() + "Service";
        } else {
            fileName = framework.getService().getServiceName();
            fileName = engine.simpleRender(fileName, metadataFinally);
        }

        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        // Si generateComponentOnly est false, on rend les fichiers additionnels
        if (!generateComponentOnly) {
            ProjectGenerator.renderFilesEdits(framework.getService().getServiceAdditionalFiles(), metadataFinally);
        }

        return result;
    }

    @Override
    public String generateController(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getControllerHashMap(framework, language, tableMetadata);
        String result = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/controllers";
        } else {
            fileSavePath = framework.getController().getControllerSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName;
        if (generateComponentOnly) {
            fileName = tableMetadata.getClassName() + "Controller";
        } else {
            fileName = framework.getController().getControllerName();
            fileName = engine.simpleRender(fileName, metadataFinally);
        }

        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        // Si generateComponentOnly est false, on rend les fichiers additionnels
        if (!generateComponentOnly) {
            ProjectGenerator.renderFilesEdits(framework.getController().getControllerAdditionalFiles(), metadataFinally);
        }

        return result;
    }

}
