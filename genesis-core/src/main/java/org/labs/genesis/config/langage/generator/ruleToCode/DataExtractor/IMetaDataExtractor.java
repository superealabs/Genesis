package org.labs.genesis.config.langage.generator.ruleToCode.DataExtractor;

import java.nio.file.Path;

public interface IMetaDataExtractor {
    String extractMetaData(Path projectBasePath, String groupId, String projectName) throws Exception;
}
