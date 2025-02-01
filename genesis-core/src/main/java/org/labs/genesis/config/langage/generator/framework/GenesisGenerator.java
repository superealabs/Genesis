package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.model.TableMetadata;

public interface GenesisGenerator {
    String generateModel(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
    String generateDao(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
    String generateService(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
    String generateController(Framework framework, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
}
