package org.labs.genesis.apj.generator;

import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.filetype.ApjFile;
import org.labs.genesis.apj.filetype.pages.PageConsulte;
import org.labs.genesis.apj.filetype.pages.PageInsert;
import org.labs.genesis.apj.filetype.pages.PageInsertMultiple;
import org.labs.genesis.apj.filetype.pages.PageRecherche;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;
import org.labs.utils.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApjFileGenerator {
    public static final GenesisTemplateEngine engine;
    public static final Map<Integer, ApjFile> apjFileMap =  new HashMap<>();

    static {
        try {
            engine = new GenesisTemplateEngine();
            PageRecherche pr = FileUtils.fromYaml(PageRecherche.class, ConstantesApj.PAGE_RECHERCHE);
            PageConsulte pc = FileUtils.fromYaml(PageConsulte.class, ConstantesApj.PAGE_CONSULTE);
            PageInsert pi = FileUtils.fromYaml(PageInsert.class, ConstantesApj.PAGE_INSERT);
            PageInsertMultiple pim = FileUtils.fromYaml(PageInsertMultiple.class, ConstantesApj.PAGE_INSERT_MULTIPLE);
            PageRecherche pro = FileUtils.fromYaml(PageRecherche.class, ConstantesApj.PAGE_RECHERCHE_ONGLET);
            PageRecherche prg = FileUtils.fromYaml(PageRecherche.class, ConstantesApj.PAGE_RECHERCHE_GROUPE);
            pro.setOnglet(true);

            apjFileMap.put(pr.getId(),pr);
            apjFileMap.put(pc.getId(),pc);
            apjFileMap.put(pi.getId(),pi);
            apjFileMap.put(pim.getId(),pim);
            apjFileMap.put(pro.getId(),pro);
            apjFileMap.put(prg.getId(),prg);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ApjFileGenerator() {
    }

    private String loadTemplate(ApjFile apjFile) throws IOException {
        return FileUtils.getFileContent(ConstantesApj.DATA_PATH + "/" + apjFile.getTemplate() + "." + Constantes.MODEL_TEMPLATE_EXT);
    }

    public void generateApjFile(ApjGenerationContext context) throws Exception {
        ApjFile apjFile = context.getApjfile();
        String templateContent = loadTemplate(apjFile);

        HashMap<String, Object> metadataPrimary = apjFile.getPrimaryHashMap();
        String result = engine.simpleRender(templateContent, metadataPrimary);

        HashMap<String, Object> metadata = apjFile.buildMetadata();
        result = engine.render(result, metadata);
        result = StringUtils.escapeAccents(result);


        FileUtils.createFile(context.getLocationDir(), apjFile.getFileName(), apjFile.getExtension(), result);
    }
}
