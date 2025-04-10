package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkConfiguration;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.model.TableMetadata;

public interface GenesisGenerator {
    String generateModel(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateDao(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateService(Framework framework, FrameworkConfiguration frameworkConfiguration, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;

    String generateController(Framework framework, FrameworkConfiguration frameworkConfiguration,  Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
}
