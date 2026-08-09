package org.labs.genesis.config.langage.generator.framework;

import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.Framework;
import org.labs.utils.FileUtils;

import java.io.IOException;

public class FrameworkTemplateLoader {
    public static String loadModelTemplate(Framework framework) throws IOException {
        return FileUtils.getFileContent(Constantes.DATA_PATH + "/" + framework.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
    }
}
