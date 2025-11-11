package org.labs.genesis.frontend.generator;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.frontend.generator.model.FrontendDestinationPaths;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;
import org.labs.utils.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.*;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;

public class ViewsGenerator implements IViewsGenerator {
    private final GenesisTemplateEngine engine;

    public ViewsGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
    }

    /**
     * Vérifie si l'authentification est activée pour le projet Django
     * @param frameworkOptions Les options du framework
     * @return true si l'authentification est activée, false sinon. Par défaut, retourne true si l'option n'est pas spécifiée.
     */
    public static boolean isAuthenticationEnabled(Map<String, Object> frameworkOptions) {
        if (frameworkOptions == null) {
            return true; // Par défaut, l'authentification est activée
        }
        
        Object enableAuthValue = frameworkOptions.get("enableAuth");
        if (enableAuthValue == null) {
            return true; // Par défaut, l'authentification est activée
        }
        
        if (enableAuthValue instanceof Boolean) {
            return (Boolean) enableAuthValue;
        }
        
        if (enableAuthValue instanceof String) {
            return Boolean.parseBoolean((String) enableAuthValue);
        }
        
        return true; // Par défaut, l'authentification est activée
    }

    @Override
    public String generateViews(FrameworkMVC framework,
                                Map<String, Object> frameworkOptions,
                                Language language,
                                ViewsTemplate viewsTemplate,
                                TableMetadata tableMetadata,
                                String destinationFolder,
                                String projectName,
                                String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        generateListView(framework, frameworkOptions, language, viewsTemplate, tableMetadata, destinationFolder, projectName, groupLink);
        generateDetailsView(framework, frameworkOptions, language, viewsTemplate, tableMetadata, destinationFolder, projectName, groupLink);

        if (Boolean.FALSE.equals(tableMetadata.getIsView())) {
            generateCreateView(framework, frameworkOptions, language, viewsTemplate, tableMetadata, destinationFolder, projectName, groupLink);
            generateEditView(framework, frameworkOptions, language, viewsTemplate, tableMetadata, destinationFolder, projectName, groupLink);
        }

        return "";
    }

    private void generateListView(FrameworkMVC framework,
                                 Map<String, Object> frameworkOptions,
                                 Language language,
                                 ViewsTemplate viewsTemplate,
                                 TableMetadata tableMetadata,
                                 String destinationFolder,
                                 String projectName,
                                 String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewListTemplate(viewsTemplate);

        HashMap<String, Object> metadataPrimary = getAltViewListHashMap(framework);
        String primaryResult = engine.simpleRender(templateContent, metadataPrimary);

        HashMap<String, Object> metadataFinally = getMvcHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

        String fileSavePath;
        fileSavePath = framework.getView().getViewSavePath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        FileUtils.createDirectory(fileSavePath);

        String fileName = framework.getView().getList().getName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(primaryResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, framework.getView().getViewExtension(), result);
    }

    private void generateDetailsView(FrameworkMVC framework,
                                 Map<String, Object> frameworkOptions,
                                 Language language,
                                 ViewsTemplate viewsTemplate,
                                 TableMetadata tableMetadata,
                                 String destinationFolder,
                                 String projectName,
                                 String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewDetailsTemplate(viewsTemplate);

        HashMap<String, Object> metadataPrimary = getAltViewDetailHashMap(framework);
        String primaryResult = engine.simpleRender(templateContent, metadataPrimary);

        HashMap<String, Object> metadataFinally = getMvcHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

        String fileSavePath;
        fileSavePath = framework.getView().getViewSavePath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        FileUtils.createDirectory(fileSavePath);

        String fileName = framework.getView().getDetail().getName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(primaryResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, framework.getView().getViewExtension(), result);
    }

    private void generateCreateView(FrameworkMVC framework,
                                    Map<String, Object> frameworkOptions,
                                    Language language,
                                    ViewsTemplate viewsTemplate,
                                    TableMetadata tableMetadata,
                                    String destinationFolder,
                                    String projectName,
                                    String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewCreateTemplate(viewsTemplate);

        HashMap<String, Object> metadataPrimary = getAltViewCreateHashMap(framework);
        String primaryResult = engine.simpleRender(templateContent, metadataPrimary);

        HashMap<String, Object> metadataFinally = getMvcHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

        String fileSavePath;
        fileSavePath = framework.getView().getViewSavePath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        FileUtils.createDirectory(fileSavePath);

        String fileName = framework.getView().getCreate().getName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(primaryResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, framework.getView().getViewExtension(), result);
    }

    private void generateEditView(FrameworkMVC framework,
                                   Map<String, Object> frameworkOptions,
                                   Language language,
                                   ViewsTemplate viewsTemplate,
                                   TableMetadata tableMetadata,
                                   String destinationFolder,
                                   String projectName,
                                   String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewEditTemplate(viewsTemplate);

        HashMap<String, Object> metadataPrimary = getAltViewEditHashMap(framework);
        String primaryResult = engine.simpleRender(templateContent, metadataPrimary);

        HashMap<String, Object> metadataFinally = getMvcHashMapIntermediaire(language, tableMetadata, framework, frameworkOptions, destinationFolder, projectName, groupLink);

        String fileSavePath;
        fileSavePath = framework.getView().getViewSavePath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        FileUtils.createDirectory(fileSavePath);

        String fileName = framework.getView().getEdit().getName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(primaryResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, framework.getView().getViewExtension(), result);
    }

    @Override
    public String generateMainLayout(FrameworkMVC framework,
                                         Map<String, Object> frameworkOptions,
                                         Language language,
                                         ViewsTemplate viewsTemplate,
                                         TableMetadata[] tableMetadata, String destinationFolder,
                                         String projectName,
                                         String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewMainLayoutTemplate(viewsTemplate);

        HashMap<String, Object> altMap = getAltViewMainLayoutHashMap(framework);
        String firstResult = engine.simpleRender(templateContent, altMap);

        HashMap<String, Object> layoutAltMap = FrameworkFrontendMetadataProvider.getLayoutHashMap(framework.getFrontendLayout());
        HashMap<String, Object> brandingAltMap = FrameworkFrontendMetadataProvider.getBrandingHashMap(framework.getProjectBranding());
        HashMap<String, Object> metadataFinally = getViewMainLayoutHashMap(framework, frameworkOptions, Arrays.stream(tableMetadata).toList(), projectName, destinationFolder, groupLink);
        metadataFinally.putAll(layoutAltMap);
        metadataFinally.putAll(brandingAltMap);

        String fileSavePath;
        fileSavePath = framework.getView().getLayout().getDestinationPath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        FileUtils.createDirectory(fileSavePath);

        String fileName = framework.getView().getLayout().getName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(firstResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, framework.getView().getViewExtension(), result);
        
        // Générer la page d'accueil
        generateHomePage(framework, frameworkOptions, language, viewsTemplate, Arrays.stream(tableMetadata).toList(), destinationFolder, projectName, groupLink);

        return "";
    }

    /**
     * Génère la page d'accueil pour Django
     */
    private void generateHomePage(FrameworkMVC framework,
                                 Map<String, Object> frameworkOptions,
                                 Language language,
                                 ViewsTemplate viewsTemplate,
                                 java.util.List<TableMetadata> entities,
                                 String destinationFolder,
                                 String projectName,
                                 String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        // Générer le template HTML de la page d'accueil
        generateHomeTemplate(framework, frameworkOptions, language, viewsTemplate, entities, destinationFolder, projectName, groupLink);
        
        // Générer les templates d'authentification uniquement si l'authentification est activée
        if (isAuthenticationEnabled(frameworkOptions)) {
            generateAuthTemplates(framework, frameworkOptions, language, viewsTemplate, destinationFolder, projectName, groupLink);
        }
    }

    /**
     * Génère le template HTML de la page d'accueil
     */
    private void generateHomeTemplate(FrameworkMVC framework,
                                     Map<String, Object> frameworkOptions,
                                     Language language,
                                     ViewsTemplate viewsTemplate,
                                     java.util.List<TableMetadata> entities,
                                     String destinationFolder,
                                     String projectName,
                                     String groupLink) throws Exception {
        String templateContent = loadViewHomeTemplate(viewsTemplate);

        // Créer un HashMap spécifique pour la page d'accueil sans dépendre d'un TableMetadata spécifique
        HashMap<String, Object> metadataFinally = getHomePageMetadata(framework, frameworkOptions, language, entities, destinationFolder, projectName, groupLink);

        // Générer le contenu des cartes de statistiques et d'actions
        String statsCards = generateStatsCards(entities);

        metadataFinally.put("statsCards", statsCards);

        String fileSavePath = framework.getView().getViewSavePath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        FileUtils.createDirectory(fileSavePath);

        String fileName = "home";
        String result = engine.render(templateContent, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, framework.getView().getViewExtension(), result);
    }

    /**
     * Génère les cartes de statistiques pour les entités
     */
    private String generateStatsCards(java.util.List<TableMetadata> entities) {
        StringBuilder cards = new StringBuilder();
        for (TableMetadata entity : entities) {
            if (!entity.getIsView()) {
                cards.append(
                    "<div class=\"stat-card\">\n" +
                    "    <div class=\"stat-content\">\n" +
                    "        <h3>" + entity.getClassName() + "s</h3>\n" +
                    "        <div class=\"stat-number\"> {{"+StringUtils.minStart(entity.getClassName()) + "_count }} </div>\n" +
                    "    </div>\n" +
                    "    <a href=\"{% url '" + StringUtils.minStart(entity.getClassName()) + "_list' %}\" class=\"stat-link\">Voir toutes</a>\n" +
                    "</div>\n"
                );
            }
        }
        return cards.toString();
    }

    /**
     * Crée les métadonnées spécifiques pour la page d'accueil
     */
    private HashMap<String, Object> getHomePageMetadata(FrameworkMVC framework,
                                                       Map<String, Object> frameworkOptions,
                                                       Language language,
                                                       java.util.List<TableMetadata> entities,
                                                       String destinationFolder,
                                                       String projectName,
                                                       String groupLink) {
        HashMap<String, Object> metadata = new HashMap<>();
        
        // Métadonnées générales
        metadata.put("destinationFolder", destinationFolder);
        metadata.put("projectName", projectName);
        metadata.put("groupLink", groupLink);
        metadata.put("groupLinkPath", groupLink != null ? groupLink.replace(".", "/") : "");
        
        // Métadonnées du framework
        metadata.putAll(getGeneralViewHashMap(framework));
        
        // Métadonnées des entités
        metadata.put("entities", getTableMetadataListForHome(entities));
        
        // Métadonnées spécifiques à la page d'accueil
        metadata.put("pkColumn", "");
        metadata.put("pkColumnType", "");
        metadata.put("tableName", "");
        metadata.put("className", "");
        metadata.put("entityName", "");
        metadata.put("classNameLink", "");
        metadata.put("isView", false);
        
        // Ajouter enableAuth dans les métadonnées (par défaut true si non spécifié)
        boolean enableAuth = true;
        if (frameworkOptions != null && frameworkOptions.containsKey("enableAuth")) {
            Object enableAuthValue = frameworkOptions.get("enableAuth");
            if (enableAuthValue instanceof Boolean) {
                enableAuth = (Boolean) enableAuthValue;
            } else if (enableAuthValue instanceof String) {
                enableAuth = Boolean.parseBoolean((String) enableAuthValue);
            }
        }
        metadata.put("enableAuth", enableAuth);
        
        return metadata;
    }

    /**
     * Crée une liste de métadonnées pour les entités de la page d'accueil
     */
    private List<Map<String, Object>> getTableMetadataListForHome(java.util.List<TableMetadata> entities) {
        List<Map<String, Object>> entityList = new ArrayList<>();
        for (TableMetadata entity : entities) {
            Map<String, Object> entityMap = getTableMetadataHashMap(entity);
            entityList.add(entityMap);
        }
        return entityList;
    }

    @Override
    public String generateErrorPage(FrameworkMVC framework,
                                         Map<String, Object> frameworkOptions,
                                         Language language,
                                         ViewsTemplate viewsTemplate,
                                         TableMetadata[] tableMetadata, String destinationFolder,
                                         String projectName,
                                         String groupLink) throws Exception {
        if (language.getId() != framework.getLanguageId()) {
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() + "' (provided ID: " + language.getId() + ") is not compatible with the framework '" + framework.getName() + "' (required language ID: '" + framework.getLanguageId() + "').");
        }

        String templateContent = loadViewErrorTemplate(viewsTemplate);

        HashMap<String, Object> altMap = getAltViewErrorHashMap(framework);
        String firstResult = engine.simpleRender(templateContent, altMap);

        HashMap<String, Object> metadataFinally = getViewMainLayoutHashMap(framework, frameworkOptions, Arrays.stream(tableMetadata).toList(), projectName, destinationFolder, groupLink);

        String fileSavePath;
        fileSavePath = framework.getView().getError().getDestinationPath();
        fileSavePath = engine.simpleRender(fileSavePath, metadataFinally);

        FileUtils.createDirectory(fileSavePath);

        String fileName = framework.getView().getError().getName();
        fileName = engine.simpleRender(fileName, metadataFinally);

        String result = engine.render(firstResult, metadataFinally);
        FileUtils.createFile(fileSavePath, fileName, framework.getView().getViewExtension(), result);

        return "";
    }

    @Override
    public  String generateResources(FrameworkMVC framework,
                                      Map<String, Object> frameworkOptions,
                                      Language language,
                                      ViewsTemplate viewsTemplate,
                                      TableMetadata[] tableMetadata, String destinationFolder,
                                      String projectName,
                                      String groupLink) throws  Exception{
        HashMap<String, Object> metadata = getHashMapIntermediaire(language, tableMetadata[0], framework, frameworkOptions, destinationFolder, projectName, groupLink);

        // Generate logo
        String logoPath = framework.getFrontendPaths().getLogoPath();
        logoPath = FrontendDestinationPaths.normalizePath(engine.simpleRender(logoPath, metadata));
        if (!framework.getProjectBranding().useLogoLink() && framework.getProjectBranding().hasLogo()){
            File logoFile = framework.getProjectBranding().getLogoFile();
            try{
                Path targetPath = Paths.get(logoPath, framework.getProjectBranding().getLogoUrl());
                Files.createDirectories(targetPath.getParent());
                Files.copy(logoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e){
                throw new Exception("Unable to upload the logo file at "+logoPath+" : "+e.getMessage(),e);
            }
        }

        // Generate favicon
        String faviconPath = framework.getFrontendPaths().getFaviconPath();
        faviconPath = FrontendDestinationPaths.normalizePath(engine.simpleRender(faviconPath, metadata));
        if (!framework.getProjectBranding().useFaviconLink() && framework.getProjectBranding().hasFavicon()){
            File faviconFile = framework.getProjectBranding().getFaviconFile();
            try{
                Path targetPath = Paths.get(faviconPath,framework.getProjectBranding().getFaviconUrl());
                Files.copy(faviconFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e){
                throw new Exception("Unable to updload the favicon at "+faviconPath+" : "+e.getMessage(),e);
            }
        }
        return "";
    }

    private String loadViewMainLayoutTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getLayoutTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewErrorTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getErrorTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewListTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getListTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewDetailsTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getDetailTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewCreateTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getCreateTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewEditTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/" + viewsTemplate.getEditTemplate() + "." + Constantes.TEMPLATE_EXT);
    }

    private String loadViewHomeTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/DjangoHomeTemplate." + Constantes.TEMPLATE_EXT);
    }

    /**
     * Génère les templates d'authentification (login et register)
     */
    private void generateAuthTemplates(FrameworkMVC framework,
                                     Map<String, Object> frameworkOptions,
                                     Language language,
                                     ViewsTemplate viewsTemplate,
                                     String destinationFolder,
                                     String projectName,
                                     String groupLink) throws Exception {
        
        // Créer le dossier auth s'il n'existe pas
        String authTemplatePath = destinationFolder + "/templates/" + projectName + "/auth";
        FileUtils.createDirectory(authTemplatePath);
        
        // Générer le template de login
        generateLoginTemplate(framework, frameworkOptions, language, viewsTemplate, destinationFolder, projectName, groupLink);
        
        // Générer le template de register
        generateRegisterTemplate(framework, frameworkOptions, language, viewsTemplate, destinationFolder, projectName, groupLink);
    }

    /**
     * Génère le template de login
     */
    private void generateLoginTemplate(FrameworkMVC framework,
                                     Map<String, Object> frameworkOptions,
                                     Language language,
                                     ViewsTemplate viewsTemplate,
                                     String destinationFolder,
                                     String projectName,
                                     String groupLink) throws Exception {
        
        Map<String, Object> metadata = getHomePageMetadata(framework, frameworkOptions, language, new ArrayList<>(), destinationFolder, projectName, groupLink);
        
        String templateContent = loadLoginTemplate(viewsTemplate);
        String renderedContent = engine.render(templateContent, metadata);

        FileUtils.createFile(destinationFolder + "/" + projectName + "/templates/" + projectName + "/auth", "login", "html", renderedContent);
    }

    /**
     * Génère le template de register
     */
    private void generateRegisterTemplate(FrameworkMVC framework,
                                        Map<String, Object> frameworkOptions,
                                        Language language,
                                        ViewsTemplate viewsTemplate,
                                        String destinationFolder,
                                        String projectName,
                                        String groupLink) throws Exception {
        
        Map<String, Object> metadata = getHomePageMetadata(framework, frameworkOptions, language, new ArrayList<>(), destinationFolder, projectName, groupLink);
        
        String templateContent = loadRegisterTemplate(viewsTemplate);
        String renderedContent = engine.render(templateContent, metadata);

        FileUtils.createFile(destinationFolder +"/"+ projectName + "/templates/" + projectName + "/auth", "register", "html", renderedContent);
    }

    private String loadLoginTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/DjangoLoginTemplate." + Constantes.TEMPLATE_EXT);
    }

    private String loadRegisterTemplate(ViewsTemplate viewsTemplate) throws IOException {
        return FileUtils.getFileContent(Constantes.TEMPLATES_PATH+ "/" + viewsTemplate.getTemplate() + "/DjangoRegisterTemplate." + Constantes.TEMPLATE_EXT);
    }
}
