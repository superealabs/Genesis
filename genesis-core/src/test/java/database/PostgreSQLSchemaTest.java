package database;

import org.junit.jupiter.api.Test;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.providers.PostgreSQLDatabase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PostgreSQLSchemaTest {
    @Test
    void includesTheConfiguredSchemaAndEscapesCredentialsInJdbcUrls() {
        var database = new PostgreSQLDatabase();
        var credentials = new Credentials().setHost("localhost").setPort("5432")
                .setDatabaseName("dashboard").setUser("test&user").setPwd("a&b=c")
                .setSchemaName("genesis_recette");
        assertThat(database.getJdbcUrl(credentials)).isEqualTo(
                "jdbc:postgresql://localhost:5432/dashboard?user=test%26user&password=a%26b%3Dc&currentSchema=genesis_recette");
        credentials.setSchemaName(null);
        assertThat(database.getJdbcUrl(credentials)).doesNotContain("currentSchema");
    }

    @Test
    void listsTablesAndViewsOnlyInTheConfiguredSchema() throws Exception {
        var database = new PostgreSQLDatabase();
        database.setCredentials(new Credentials().setSchemaName("genesis_recette"));
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getTables(isNull(), eq("genesis_recette"), eq("%"), any(String[].class)))
                .thenAnswer(invocation -> mock(ResultSet.class));
        assertThat(database.getAllTableNames(connection)).isEmpty();
        assertThat(database.getAllViewNames(connection)).isEmpty();
        verify(metadata).getTables(null, "genesis_recette", "%", new String[]{"TABLE"});
        verify(metadata).getTables(null, "genesis_recette", "%", new String[]{"VIEW"});
    }
}
