package project;

import org.junit.jupiter.api.Test;
import org.labs.genesis.apj.affichage.gen.PageRechercheGen;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public class GenesisAPJCoreTest {

    public static HashMap<String, Object> getPageRecherchePrimaryHashMap(PageRechercheGen prg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("imports", prg.getImports());
        map.put("pr", prg.getPr());
        map.put("html", prg.getHtml());
        map.put("basPage", prg.getBasPage());
        return map;
    }

    public static HashMap<String, Object> getPageRechercheHashMap(PageRechercheGen prg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("packageMapping", prg.getPackageMapping());
        map.put("mapping", prg.getMapping());
        map.put("nomTable", prg.getNomTable());
        map.put("listeCrt", prg.getListeCrt());
        map.put("listeInt", prg.getListeInt());
        map.put("colSomme", prg.getColSomme());
        map.put("libEntete", prg.getLibEntete());
        map.put("titre", prg.getTitre());
        map.put("apres", prg.getApres());
        map.put("libEnteteAffiche", prg.getLibEnteteAffiche());
        return map;
    }

    public static String quoteAndJoin(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        return Arrays.stream(array)
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(", "));
    }

    @Test
    public void genererFichierAPJ() throws IOException {
//        String fileSavePath = "../generated/apj";
        String fileSavePath = "/home/antema/Antema/BICI/Antema/APJ/hatana/socobis-war/web/pages/annexe/produit";
        PageRechercheGen[] pages = FileUtils.fromYaml(PageRechercheGen[].class, Constantes.PROJECT_APJ_YAML);
        Map<Integer, PageRechercheGen> projects = Arrays.stream(pages)
                .collect(Collectors.toMap(PageRechercheGen::getId, page -> page));
        System.out.println("coucou");

        PageRechercheGen pr = projects.get(1);
        pr.setPackageMapping("produits.IngredientsLib");
        pr.setMapping("IngredientsLib");
        pr.setNomTable("AS_INGREDIENTS_LIB_DATY");
        pr.setTitre("Test Kely Liste");
        pr.setApres("annexe/produit/produit-liste.jsp");
        pr.setColSomme("null");
        String[] listeCrt = {"id", "libelle","idcategorie","unite"};
        String[] listeInt = {};
        String[] libEntete = {"id", "libelle","categorieingredient","unite"};
        String[] libEnteteAffiche = {"ID", "D&eacute;signation","Cat&eacute;gorie","Unit&eacute;"};

        pr.setListeCrt(quoteAndJoin(listeCrt));
        pr.setListeInt(quoteAndJoin(listeInt));
        pr.setLibEntete(quoteAndJoin(libEntete));
        pr.setLibEnteteAffiche(quoteAndJoin(libEnteteAffiche));

        HashMap<String, Object> metadataPrimary = getPageRecherchePrimaryHashMap(pr);
        String templateContent = FileUtils.getFileContent(Constantes.DATA_PATH + "/pr" + "." + Constantes.MODEL_TEMPLATE_EXT);
        GenesisTemplateEngine engine = new GenesisTemplateEngine();
        String result = engine.simpleRender(templateContent, metadataPrimary);
        HashMap<String, Object> metadata = getPageRechercheHashMap(pr);
        result = engine.simpleRender(result, metadata);

        FileUtils.createFile(fileSavePath, "test-liste", "jsp", result);
    }

    @Test
    void testClassloader() throws Exception {
        File classesDir = new File("/home/antema/Antema/BICI/Antema/APJ/hatana/build-file/socobis_jar/");
        File libDir = new File("/home/antema/Antema/BICI/Antema/APJ/hatana/build-file/lib/");

        File[] jarFiles = libDir.listFiles(f -> f.getName().endsWith(".jar"));
        int urlsLength = (jarFiles != null ? jarFiles.length : 0) + 1;
        URL[] urls = new URL[urlsLength];

        urls[0] = classesDir.toURI().toURL();
        if (jarFiles != null) {
            for (int i = 0; i < jarFiles.length; i++) {
                urls[i + 1] = jarFiles[i].toURI().toURL();
            }
        }
        Class<?> cls = null;
        try {
            URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
            cls = loader.loadClass("produits.IngredientsLib");
        } catch (ClassNotFoundException e) {
            throw new Exception("Classe introuvable : produits.IngredentsLib", e);
        }
        Class<?> current = cls;
        List<Field> attributs = new ArrayList<>();
        while (current != null && !current.getSimpleName().equals("ClassMAPTable")) {
            Field[] fields = current.getDeclaredFields();
            Collections.addAll(attributs, fields);
            current = current.getSuperclass();
        }
        System.out.println("----------------------------------");
        for (Field field : attributs) {
            System.out.println(field.getName());
        }
        System.out.println("==================================");

        try (Connection conn = UtilDBDynamique.GetConn(classesDir, libDir)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String tableName = "AS_INGREDIENTS_VENTE";

            try (ResultSet columns = metaData.getColumns(null, null, tableName.toUpperCase(), null)) {
                if (!columns.next()) {
                    System.out.println("Table ou vue '" + tableName + "' inexistante pour l'utilisateur connecté.");
                } else {
                    System.out.println("Colonnes de " + tableName + ":");
                    do {
                        String name = columns.getString("COLUMN_NAME");
                        String type = columns.getString("TYPE_NAME");
                        int size = columns.getInt("COLUMN_SIZE");
                        System.out.printf("%s - %s(%d)%n", name, type, size);
                    } while (columns.next());
                }
            }

        }

    }

    @Test
    void testConnexionDynamique() throws Exception {
        File socobisJar = new File("/home/antema/Antema/BICI/Antema/APJ/hatana/build-file/socobis_jar/");
        File libDir = new File("/home/antema/Antema/BICI/Antema/APJ/hatana/build-file/lib/");

        try (Connection conn = UtilDBDynamique.GetConn(socobisJar, libDir)) {
            System.out.println("Connexion réussie via UtilDBDynamique !");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
