package org.labs.genesis.config.langage.generator.sub;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.sub.models.GenesisFileModel;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.Map;

@Getter
@Setter
public class SubGenerator {
    ProjectGenerationContext context;
    public SubGenerator(ProjectGenerationContext context) {
        setContext(context);
    }
    public void generateGenesisfile() throws Exception {
        GenesisFileModel genesisFileModel = new GenesisFileModel(context);
        String finalContent = FileUtils.toJsonString(genesisFileModel);
        FileUtils.createFile(context.getDestinationFolder(),"genesis-context","json", finalContent);
    }
}
