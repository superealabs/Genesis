package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.langage.*;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.Map;

public interface GenesisGenerator {
    String generateModel(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateDao(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateService(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateController(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    default  String generateViews(FrameworkMVC framework,
                                  Map<String, Object> frameworkOptions,
                                  Language language,
                                  ViewsTemplate viewsTemplate,
                                  ViewsTemplateEngine viewsTemplateEngine,
                                  TableMetadata tableMetadata,
                                  String destinationFolder,
                                  String projectName,
                                  String groupLink) throws Exception {
        return null;
    }

    default  String generateViewMainLayout(FrameworkMVC framework,
                                           Map<String, Object> frameworkOptions,
                                           Language language,
                                           ViewsTemplate viewsTemplate,
                                           ViewsTemplateEngine viewsTemplateEngine,
                                           TableMetadata[] tableMetadata,
                                           String destinationFolder,
                                           String projectName,
                                           String groupLink) throws Exception {
        return null;
    }

    default  String generateViewErrorPage(FrameworkMVC framework,
                                           Map<String, Object> frameworkOptions,
                                           Language language,
                                           ViewsTemplate viewsTemplate,
                                           ViewsTemplateEngine viewsTemplateEngine,
                                           TableMetadata[] tableMetadata,
                                           String destinationFolder,
                                           String projectName,
                                           String groupLink) throws Exception {
        return null;
    }
}
