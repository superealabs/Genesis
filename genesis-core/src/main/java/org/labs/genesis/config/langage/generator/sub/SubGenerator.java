package org.labs.genesis.config.langage.generator.sub;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
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

        // build the metadata
        Map<String, Object> metadata = GeneratorMetadataProvider.getGenesisFileMetadata(context);
        // create the template
        String template = "";
        try {
            template = this.loadGenesisfileTemplate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // generete the file
        String finalContent = ProjectGenerator.engine.render(template, metadata);
        FileUtils.createFile(context.getDestinationFolder(),"genesis","json", finalContent);
    }

    private String loadGenesisfileTemplate() throws IOException {
        return FileUtils.getFileContent(Constantes.GENESIS_FILE_PATH+ "." + Constantes.MODEL_TEMPLATE_EXT);
    }
}
