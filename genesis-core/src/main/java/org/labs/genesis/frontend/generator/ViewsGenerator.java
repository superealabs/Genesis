package org.labs.genesis.frontend.generator;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.*;
import org.labs.genesis.frontend.generator.model.FrontendDestinationPaths;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider.*;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;

public class ViewsGenerator implements IViewsGenerator {
    private final GenesisTemplateEngine engine;

    public ViewsGenerator(GenesisTemplateEngine engine) {
        this.engine = engine;
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

        return "";
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
}
