package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.*;

public class APIGenerator implements GenesisGenerator {
    private final GenesisTemplateEngine engine;

    public APIGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }


    private String loadTemplate(Framework framework) throws IOException {
        return FileUtils.getFileContent(Constantes.DATA_PATH + "/" + framework.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
    }

    private String loadViewTemplate(UIViews uiViews, String uiElementsType) throws IOException {
        return FileUtils.getFileContent(Constantes.LAYOUT_DATA_PATH + "/" + uiElementsType + "/" + uiViews.getTemplate() + "." + Constantes.VIEWS_TEMPLATE_EXT);
    }

    private String loadListViewTemplate(UIViews uiViews, String uiElementsType) throws IOException {
        return FileUtils.getFileContent(Constantes.LAYOUT_DATA_PATH + "/" + uiElementsType + "/" + uiViews.getListTemplate() + "." + Constantes.VIEWS_TEMPLATE_EXT);
    }

    private String loadCreateViewTemplate(UIViews uiViews, String uiElementsType) throws IOException {
        return FileUtils.getFileContent(Constantes.LAYOUT_DATA_PATH + "/" + uiElementsType + "/" + uiViews.getCreateTemplate() + "." + Constantes.VIEWS_TEMPLATE_EXT);
    }

    @Override
    public String generateModel(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getModelHashMap(frameworkConfiguration, language, tableMetadata);
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
            fileSavePath = frameworkConfiguration.getModel().getModelSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier correspondant
        String fileName = tableMetadata.getClassName();
        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        ProjectGenerator.renderFilesEdits(frameworkConfiguration.getModel().getModelAdditionalFiles(), metadataFinally);

        return result;
    }

    @Override
    public String generateDao(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getModelDaoHashMap(frameworkConfiguration, language, tableMetadata);
        String result = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);
        metadataFinally.putAll(getPrimaryModelDaoHashMap(frameworkConfiguration, tableMetadata));

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/repositories";
        } else {
            fileSavePath = frameworkConfiguration.getModelDao().getModelDaoSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName;
        if (generateComponentOnly) {
            fileName = tableMetadata.getClassName() + "Repository";
        } else {
            fileName = frameworkConfiguration.getModelDao().getModelDaoName();
            fileName = engine.simpleRender(fileName, metadataFinally);
        }

        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        // Si generateComponentOnly est false, on rend les fichiers additionnels
        if (!generateComponentOnly) {
            ProjectGenerator.renderFilesEdits(frameworkConfiguration.getModelDao().getModelDaoAdditionalFiles(), metadataFinally);
        }

        return result;
    }

    @Override
    public String generateService(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getServiceHashMap(frameworkConfiguration, language, tableMetadata);
        String result = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/services";
        } else {
            fileSavePath = frameworkConfiguration.getService().getServiceSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName;
        if (generateComponentOnly) {
            fileName = tableMetadata.getClassName() + "Service";
        } else {
            fileName = frameworkConfiguration.getService().getServiceName();
            fileName = engine.simpleRender(fileName, metadataFinally);
        }

        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        // Si generateComponentOnly est false, on rend les fichiers additionnels
        if (!generateComponentOnly) {
            ProjectGenerator.renderFilesEdits(frameworkConfiguration.getService().getServiceAdditionalFiles(), metadataFinally);
        }

        return result;
    }

    @Override
    public String generateController(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Chargement du template
        String templateContent = loadTemplate(framework);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getControllerHashMap(frameworkConfiguration, language, tableMetadata);
        String result = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        if (generateComponentOnly) {
            fileSavePath = destinationFolder + "/" + projectName + "/controllers";
        } else {
            fileSavePath = frameworkConfiguration.getController().getControllerSavePath();
            fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);
        }

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName;
        if (generateComponentOnly) {
            fileName = tableMetadata.getClassName() + "Controller";
        } else {
            fileName = frameworkConfiguration.getController().getControllerName();
            fileName = engine.simpleRender(fileName, metadataFinally);
        }

        result = engine.render(result, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, language.getExtension(), result);

        // Si generateComponentOnly est false, on rend les fichiers additionnels
        if (!generateComponentOnly) {
            ProjectGenerator.renderFilesEdits(frameworkConfiguration.getController().getControllerAdditionalFiles(), metadataFinally);
        }

        return result;
    }

    public void generateCreateView(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee, Framework frame, FrameworkConfiguration framework, Language language, UIViews uiViews, UIViewsConfiguration uiViewsConfiguration, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception {
        if (language.getId() != frame.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + frame.getLanguageId() + "').");
        }

        String templateContent = loadCreateViewTemplate(uiViews, uiViews.getFileName());

        // Render les attributs specifiques
        HashMap<String, Object> altCreateMap = ProjectMetadataProvider.getAltCreateViewHashMap(uiViewsConfiguration);
        String firstResult = engine.simpleRenderAlt(templateContent, altCreateMap);

        // Render les attributs intermediaires
        HashMap<String, Object> intermed = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);
        String secondResult = engine.simpleRender(firstResult, intermed);

        // Rendue final
        HashMap<String, Object> metadataFinally = getAllCreateViewHashMap(columnMetadatas, metaDonnee, framework, uiViewsConfiguration, tableMetadata, destinationFolder, projectName, groupLink);
        String result = engine.render(secondResult, metadataFinally);

        StringBuilder resultCleaned = new StringBuilder(result);
        engine.dropCommentary(resultCleaned);

        String fileName = framework.getView().getCreateViewName();
        String fileSavePath = framework.getView().getViewSavePath();
        String fileExtension = framework.getView().getViewExtension();
        FileUtils.createFile(engine.simpleRender(fileSavePath, metadataFinally), engine.simpleRender(fileName, metadataFinally), engine.simpleRender(fileExtension, metadataFinally), result);

    }

    public void generateListView(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee, Framework frame, FrameworkConfiguration framework, Language language, UIViews uiViews, UIViewsConfiguration uiViewsConfiguration, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception {
        if (language.getId() != frame.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + frame.getLanguageId() + "').");
        }

        String templateContent = loadListViewTemplate(uiViews, uiViews.getFileName());

        // Render les attributs specifiques
        HashMap<String, Object> altMap = ProjectMetadataProvider.getAltListViewHashMap(uiViewsConfiguration);
        String firstResult = engine.simpleRenderAlt(templateContent, altMap);

        // Render les attributs intermediaires
        HashMap<String, Object> intermed = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);
        String secondResult = engine.simpleRender(firstResult, intermed);

        // Rendue final
        HashMap<String, Object> metadataFinally = getAllListViewHashMap(columnMetadatas, metaDonnee, framework, uiViewsConfiguration, tableMetadata, destinationFolder, projectName, groupLink);
        String result = engine.render(secondResult, metadataFinally);

        StringBuilder resultCleaned = new StringBuilder(result);
        engine.dropCommentary(resultCleaned);

        String fileName = framework.getView().getListViewName();
        String fileSavePath = framework.getView().getViewSavePath();
        String fileExtension = framework.getView().getViewExtension();
        FileUtils.createFile(engine.simpleRender(fileSavePath, metadataFinally), engine.simpleRender(fileName, metadataFinally), engine.simpleRender(fileExtension, metadataFinally), result);
    }

    @Override
    public String generateView(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee,Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, UIViews uiViews, UIViewsConfiguration uiViewsConfiguration, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        generateListView(columnMetadatas, metaDonnee, framework, frameworkConfiguration, language, uiViews, uiViewsConfiguration, tableMetadata, destinationFolder, projectName, groupLink);
        generateCreateView(columnMetadatas, metaDonnee, framework, frameworkConfiguration, language, uiViews, uiViewsConfiguration, tableMetadata, destinationFolder, projectName, groupLink);

        return "";
    }

    @Override
    public void generateViewMainLayout(Framework framework, Language language, UIViews uiViews, UIViewsConfiguration uiViewsConfiguration, TableMetadata[] tableMetadatas, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception {
        // Vérification de compatibilité
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the framework '" +
                    framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewTemplate(uiViews, uiViews.getFileName());

        //render base layout
        HashMap<String, Object> metadata = getViewMainLayoutHashMap(tableMetadatas, tableMetadata, language, uiViewsConfiguration);

        String result = engine.simpleRender(templateContent, metadata);

        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, destinationFolder, projectName, groupLink);

        result = engine.render(result, metadataFinally);

        metadataFinally.putAll(metadata);
        FileUtils.overwriteFileContentByName(engine.simpleRender(uiViewsConfiguration.getLayout().getDestinationPath(), metadataFinally), uiViewsConfiguration.getLayout().getName(), result);

    }

}
