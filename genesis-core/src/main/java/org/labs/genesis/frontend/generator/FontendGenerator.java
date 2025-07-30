package org.labs.genesis.frontend.generator;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.frameworkFrontend.FrameworkFrontendMetadataProvider;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.util.HashMap;

public class FontendGenerator implements IFrontendGenerator{
    private final GenesisTemplateEngine engine;

    FontendGenerator(GenesisTemplateEngine engine) {
        this.engine=engine;
    }

    @Override
    public String generateComponent(FrontendLanguage language, FrontendFramework frontendFramework,TableMetadata tableMetadata)throws Exception {
        if(language.getId()!=frontendFramework.getLanguageId()){
            throw new RuntimeException("Incompatibility detected: the language '" + language.getName() +
                    "' (provided ID: " + language.getId() + ") is not compatible with the frontend framework '" +
                    frontendFramework.getName() + "' (required language ID: '" + frontendFramework.getLanguageId() + "').");
        }
        String templateArchitecture=loadTemplate(frontendFramework);

        HashMap<String,Object> metadataPrimaryForList= FrameworkFrontendMetadataProvider.getComponentHashMapList(frontendFramework,language,tableMetadata);
        String resultForListComponent=engine.simpleRender(templateArchitecture,metadataPrimaryForList);

        HashMap<String,Object> metadataPrimaryForForm= FrameworkFrontendMetadataProvider.getComponentHashMapList(frontendFramework,language,tableMetadata);
        String resultForFormComponent=engine.simpleRender(templateArchitecture,metadataPrimaryForForm);

        return "";
    }

    @Override
    public String generateService(FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata)throws Exception {
        return "";
    }

    @Override
    public String generateModel(FrontendLanguage language,FrontendFramework frontendFramework, TableMetadata tableMetadata) throws Exception{
        return "";
    }


    private String loadTemplate(FrontendFramework frontendFramework) throws IOException {
        return FileUtils.getFileContent(Constantes.DATA_PATH + "/" + frontendFramework.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
    }
}
