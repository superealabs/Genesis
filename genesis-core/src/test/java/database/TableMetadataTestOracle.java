
package database;

import org.junit.jupiter.api.Test;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.model.TableMetadata;

import java.sql.Connection;
import java.util.Arrays;

public class TableMetadataTestOracle {
    Credentials credentials;

    public TableMetadataTestOracle() {
        this.credentials = new Credentials()
                .setHost("localhost")
                .setPort("1521")
                .setUser("C##TAHIANA")
                .setPwd("tahiana")
                .setSID("orclbdd");
    }


    @Test
    void listTableMetadataFixTroubleOracle() {
        int databaseId = Constantes.Oracle_ID;
        int languageId = Constantes.Java_ID;
        int frameworkId=Constantes.Spring_REST_API_ID;

        var database = ProjectGenerator.databases.get(databaseId);
        var language = ProjectGenerator.languages.get(languageId);
        var  framework= ProjectGenerator.frameworks.get(frameworkId);

        try (Connection connection = database.getConnection(credentials)) {
            TableMetadata[] entities = database.getEntities(connection, credentials, language,framework).toArray(new TableMetadata[0]);
            System.out.println("\n\nEntities : \n" + Arrays.toString(entities) + "\n\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    @Test
    void listTableMetadata() {
        int databaseId = Constantes.Oracle_ID;
        int languageId = Constantes.Java_ID;
        int frameworkId=Constantes.Spring_REST_API_ID;

        var database = ProjectGenerator.databases.get(databaseId);
        var language = ProjectGenerator.languages.get(languageId);
        var  framework= ProjectGenerator.frameworks.get(frameworkId);

        try (Connection connection = database.getConnection(credentials)) {
            TableMetadata[] entities = database.getEntities(connection, credentials, language,framework).toArray(new TableMetadata[0]);
            System.out.println("\n\nEntities : \n" + Arrays.toString(entities) + "\n\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void listViewMetadata() {
        int databaseId = Constantes.Oracle_ID;
        int languageId = Constantes.Java_ID;
        int frameworkId=Constantes.Spring_REST_API_ID;


        var database = ProjectGenerator.databases.get(databaseId);
        var language = ProjectGenerator.languages.get(languageId);
        var  framework= ProjectGenerator.frameworks.get(frameworkId);

        try (Connection connection = database.getConnection(credentials)) {
            TableMetadata[] entities = database.getViews(connection, credentials, language,framework).toArray(new TableMetadata[0]);
            System.out.println("\n\nEntities : \n" + Arrays.toString(entities) + "\n\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}