package org.labs.genesis.config.langage.generator.sync.loader;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.generator.sync.models.GenesisContextModel;
import org.labs.utils.FileUtils;

public class GenesisContextLoader {
    public GenesisContextModel loadContextModel(String projectPath) throws Exception {
        String filePath = projectPath +"/"+ Constantes.GENESIS_CONTEXT_FILE;
        GenesisContextModel fileModel = FileUtils.fromJson(GenesisContextModel.class, filePath);
        return fileModel;
    }
}
