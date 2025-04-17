package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.langage.*;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

public interface GenesisGenerator {
    String generateModel(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateDao(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateService(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateController(Framework framework, FrameworkConfiguration frameworkConfiguration,  Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateView(ColumnMetadata[] columnMetadatas, ColumnMetadata metaDonnee, Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, UIViews uiViews, UIViewsConfiguration uiViewsConfiguration, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    void generateViewMainLayout(Framework framework, Language language, UIViews uiViews, UIViewsConfiguration uiViewsConfiguration, TableMetadata[] tableMetadatas, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink) throws Exception;

}
