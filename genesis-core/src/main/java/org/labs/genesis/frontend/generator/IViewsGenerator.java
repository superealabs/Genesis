package org.labs.genesis.frontend.generator;

import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.ViewsTemplate;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.Map;

public interface IViewsGenerator {
    String generateViews(FrameworkMVC framework, Map<String, Object> frameworkOptions, Language language, ViewsTemplate viewsTemplate, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception;
    String generateMainLayout(FrameworkMVC framework, Map<String, Object> frameworkOptions, Language language, ViewsTemplate viewsTemplate, TableMetadata[] tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception;
    String generateErrorPage(FrameworkMVC framework, Map<String, Object> frameworkOptions, Language language, ViewsTemplate viewsTemplate, TableMetadata[] tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception;
    String generateResources(FrameworkMVC framework, Map<String, Object> frameworkOptions, Language language, ViewsTemplate viewsTemplate, TableMetadata[] tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception;
}
