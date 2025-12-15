package project;

import org.junit.jupiter.api.Test;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.apj.filetype.pages.PageRecherche;
import org.labs.genesis.apj.generator.ApjFileGenerator;
import org.labs.genesis.apj.utilitaire.ConstantesApj;
import org.labs.genesis.apj.utilitaire.UtilClassLoader;
import org.labs.genesis.apj.utilitaire.UtilDBDynamique;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.utils.FileUtils;
import org.labs.utils.StringUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;
import org.labs.genesis.apj.utilitaire.Database;

public class GenesisAPJCoreTest {

    @Test
    public void genererFichierAPJ() throws Exception {
        ApjFileGenerator generator = new ApjFileGenerator();
        ApjGenerationContext context = new ApjGenerationContext();

        PageRecherche pr = (PageRecherche) ApjFileGenerator.apjFileMap.get(ConstantesApj.PAGE_RECHERCHE_ID);
        pr.setFileName("ingredient-liste");
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

        pr.setListeCrt(StringUtils.quoteAndJoin(listeCrt));
        pr.setListeInt(StringUtils.quoteAndJoin(listeInt));
        pr.setLibEntete(StringUtils.quoteAndJoin(libEntete));
        pr.setLibEnteteAffiche(StringUtils.quoteAndJoin(libEnteteAffiche));

        String fileSavePath = "/home/antema/Antema/BICI/Antema/APJ/hatana/socobis-war/web/pages/annexe/produit/test";
        context.setLocationDir(fileSavePath);
        context.setApjfile(pr);
        generator.generateApjFile(context);
    }

    @Test
    void testUtilClassLoader() throws Exception {
        File classesDir = new File("/home/antema/Antema/BICI/Antema/APJ/hatana/build-file/socobis_jar/");
        File libDir = new File("/home/antema/Antema/BICI/Antema/APJ/hatana/build-file/lib/");

        URLClassLoader loader = UtilClassLoader.buildLoader(classesDir, libDir);
        Class<?> cls = UtilClassLoader.loadClass(loader, "produits.IngredientsLib");

        List<Field> attributs = UtilClassLoader.listFields(cls, "ClassMAPTable");
        for (Field f : attributs) {
            System.out.println(f.getName());
        }

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


    @Test
    public void testGenererLibelles() throws Exception {
        ApjField[] fields = {
            new ApjField("idhistorique", "String"),
            new ApjField("heure", "String"),
            new ApjField("objet", "String"),
            new ApjField("action", "double"),
            new ApjField("idutilisateur", "String"),
            new ApjField("refobjet", "String"),
            new ApjField("depense", "String"),
            new ApjField("datehistorique", "Date")
        };
        String mapping = "produits.Historique";
        LlmApiClient llmClient = new LlmApiClient();
        String[] libelles = llmClient.askForLabel(mapping, fields,ConstantesApj.STANDARD);

        for (String libelle : libelles) {
            System.out.println(libelle);
        }
    }

    @Test
    public void testPath(){
        String root = "/home/antema/Antema/BICI/Socobis/socobis/socobis-war/web/pages";
        String file = "/home/antema/Antema/BICI/Socobis/socobis/socobis-war/web/pages/analyse/mon-fichier";

        String result = StringUtils.relativeOrFilename(root, file);
        System.out.println(result);
    }

    @Test
    void testGetTableColumnsDisplay() throws Exception {
        File classesDir = new File("/home/antema/Antema/BICI/Socobis/socobis/build-file/socobis_jar/");
        File libDir = new File("/home/antema/Antema/BICI/Socobis/socobis/build-file/lib/");

        try (Connection conn = UtilDBDynamique.GetConn(classesDir, libDir)) {

            String tableName = "UNITE";

            List<ApjField> fields = Database.getTableColumns(conn, tableName);

            System.out.println("Colonnes de la table " + tableName + " :\n");

            for (ApjField field : fields) {
                System.out.println(field.getNom());
                System.out.println(field.getType());
                System.out.println(field.getTypeBase());
                System.out.println(field.getNomBase());
                System.out.println("===================================");
            }
        }
    }


    @Test
    public void test() throws IOException {
        Map<Integer, Database> databases;
        databases = Arrays.stream(FileUtils.fromJson(Database[].class, Constantes.DATABASE_JSON))
                .collect(Collectors.toMap(Database::getId, database -> database));
        System.out.println("test");
    }


    @Test
    public void testExtractPackage(){
        String root = "/home/user/project/src";
        String fullPath = "/home/user/project/src/org/labs/genesis/apj/utilitaire/Database.java";

        String pkg = StringUtils.getPackageFromFile(root, fullPath);
        System.out.println(pkg);

    }
}
