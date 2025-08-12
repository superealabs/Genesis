package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.project.ProjectMetadataProvider;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.*;

public class MVCGenerator implements GenesisGenerator {
    private final GenesisTemplateEngine engine;

    public MVCGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    private String loadTemplate(Framework framework) throws IOException {
        return FileUtils.getFileContent(Constantes.DATA_PATH + "/" + framework.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
    }

    @Override
    public String generateModel(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
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
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

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

        ProjectGenerator.renderFilesEdits(framework.getModel().getModelAdditionalFiles(), metadataFinally);

        return result;
    }

    @Override
    public String generateDao(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
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
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);
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
    public String generateService(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
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
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

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
    public String generateController(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception {
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
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

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

    public String generateViews(FrameworkMVC framework,
                                Map<String, Object> frameworkOptions,
                                Language language,
                                ViewsTemplate viewsTemplate,
                                ViewsTemplateEngine viewsTemplateEngine,
                                TableMetadata tableMetadata,
                                String destinationFolder,
                                String projectName,
                                String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        generateListView(framework, frameworkOptions, language, viewsTemplate, viewsTemplateEngine, tableMetadata, destinationFolder, projectName, groupLink);
        generateDetailsView(framework, frameworkOptions, language, viewsTemplate, viewsTemplateEngine, tableMetadata, destinationFolder, projectName, groupLink);

        return "";
    }

    public void generateListView(FrameworkMVC framework,
                                 Map<String, Object> frameworkOptions,
                                 Language language,
                                 ViewsTemplate viewsTemplate,
                                 ViewsTemplateEngine viewsTemplateEngine,
                                 TableMetadata tableMetadata,
                                 String destinationFolder,
                                 String projectName,
                                 String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewListTemplate(viewsTemplate);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getViewHashMap(framework, language, tableMetadata);
        String primaryResult = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu intermédiaire
        HashMap<String, Object> metadataSecondary = ProjectMetadataProvider.getAltViewListHashMap(viewsTemplateEngine);
        String secondaryResult = engine.simpleRender(primaryResult, metadataSecondary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        fileSavePath = framework.getView().getViewSavePath() + "/" + tableMetadata.getClassName();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName = framework.getView().getListViewName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(secondaryResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, viewsTemplateEngine.getViewExtension(), result);
    }


    public void generateDetailsView(FrameworkMVC framework,
                                 Map<String, Object> frameworkOptions,
                                 Language language,
                                 ViewsTemplate viewsTemplate,
                                 ViewsTemplateEngine viewsTemplateEngine,
                                 TableMetadata tableMetadata,
                                 String destinationFolder,
                                 String projectName,
                                 String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewDetailsTemplate(viewsTemplate);

        // Rendu intermédiaire
        HashMap<String, Object> metadataPrimary = getViewHashMap(framework, language, tableMetadata);
        String primaryResult = engine.simpleRender(templateContent, metadataPrimary);

        // Rendu intermédiaire
        HashMap<String, Object> metadataSecondary = ProjectMetadataProvider.getAltViewDetailHashMap(viewsTemplateEngine);
        String secondaryResult = engine.simpleRender(primaryResult, metadataSecondary);

        // Rendu final
        HashMap<String, Object> metadataFinally = getHashMapIntermediaire(tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        fileSavePath = framework.getView().getViewSavePath() + "/" + tableMetadata.getClassName();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName = framework.getView().getDetailViewName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(secondaryResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, viewsTemplateEngine.getViewExtension(), result);
    }

    @Override
    public String generateViewMainLayout(FrameworkMVC framework,
                                         Map<String, Object> frameworkOptions,
                                         Language language,
                                         ViewsTemplate viewsTemplate,
                                         ViewsTemplateEngine viewsTemplateEngine,
                                         TableMetadata[] tableMetadata, String destinationFolder,
                                         String projectName,
                                         String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewMainLayoutTemplate(viewsTemplate);

        HashMap<String, Object> altMap = ProjectMetadataProvider.getAltViewMainLayoutHashMap(viewsTemplateEngine);
        String firstResult = engine.simpleRender(templateContent, altMap);

        HashMap<String, Object> metadataFinally = getViewMainLayoutHashMap(framework, frameworkOptions, Arrays.stream(tableMetadata).toList(), projectName, destinationFolder, groupLink);

        // Ajustement du chemin de sauvegarde
        String fileSavePath;
        fileSavePath = viewsTemplateEngine.getLayout().getDestinationPath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        // S'assurer que le répertoire existe
        FileUtils.createDirectory(fileSavePath);

        // Création du fichier
        String fileName = viewsTemplateEngine.getLayout().getName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(firstResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, viewsTemplateEngine.getViewExtension(), result);

        return "";
    }

    private String loadViewMainLayoutTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getLayoutTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewListTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getListTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewDetailsTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getDetailTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

}
