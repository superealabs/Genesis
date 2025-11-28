package org.labs.genesis.remover;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.Map;

public interface IAPIRemover {
    String removeModel(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
    String removeDao(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
    String removeService(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
    String removeController(Framework framework, Map<String, Object> frameworkOptions, Language language, TableMetadata tableMetadata, String destinationFolder, String projectName, String groupLink, boolean generateComponentOnly) throws Exception;
}
