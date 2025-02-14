package database;

import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.model.TableMetadata;

import java.sql.Connection;
import java.util.Arrays;

public class TableMetadataTestMysql {

    Credentials credentials;

    public TableMetadataTestMysql() {
        this.credentials = new Credentials()
                .setHost("localhost")
                .setPort("3306")
                .setDatabaseName("biblio")
                .setUser("root")
                .setPwd("");
    }


    @Test
    void listTableMetadata() {
        int databaseId = Constantes.MySQL_ID;
        int languageId = Constantes.Java_ID;

        var database = ProjectGenerator.databases.get(databaseId);
        var language = ProjectGenerator.languages.get(languageId);

        try (Connection connection = database.getConnection(credentials)) {
            TableMetadata[] entities = database.getEntities(connection, credentials, language).toArray(new TableMetadata[0]);
            System.out.println("\n\nEntities : \n"+Arrays.toString(entities)+"\n\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void listViewMetadata() {
        int databaseId = Constantes.MySQL_ID;
        int languageId = Constantes.Java_ID;

        var database = ProjectGenerator.databases.get(databaseId);
        var language = ProjectGenerator.languages.get(languageId);

        try (Connection connection = database.getConnection(credentials)) {
            TableMetadata[] entities = database.getViews(connection, credentials, language).toArray(new TableMetadata[0]);
            System.out.println("\n\nEntities : \n"+Arrays.toString(entities)+"\n\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
