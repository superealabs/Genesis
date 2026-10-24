package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.SqlScriptDtos.*;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.Credentials;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Connection;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqlScriptServiceTest {
    @Test
    void rejectsMissingConfirmationBeforeOpeningConnection() {
        var store = new GenerationContextStore();
        var database = mock(Database.class);
        store.getContext().setDatabase(database);
        var service = new SqlScriptService(store, true);
        assertThatThrownBy(() -> service.execute(new SqlScriptRequest("SELECT 1;", false)))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode().value()).isEqualTo(400));
        verifyNoInteractions(database);
    }

    @Test
    void savingScriptNeverOpensConnection() {
        var store = new GenerationContextStore();
        var database = mock(Database.class);
        store.getContext().setDatabase(database);
        var service = new SqlScriptService(store, true);
        assertThat(service.saveScriptConfig(new ScriptConfigRequest("", "CREATE TABLE test(id INT);"))
                .success()).isTrue();
        verifyNoInteractions(database);
    }

    @Test
    void sqlFailureRollsBackAndReturnsBadRequestWithoutDatabaseDetails() throws Exception {
        var store = new GenerationContextStore();
        var database = mock(Database.class);
        var credentials = new Credentials();
        var connection = mock(Connection.class);
        store.getContext().setDatabase(database).setCredentials(credentials);
        when(database.getConnection(credentials)).thenReturn(connection);
        when(connection.createStatement()).thenThrow(new SQLException("private database details"));
        var service = new SqlScriptService(store, true);
        assertThatThrownBy(() -> service.execute(new SqlScriptRequest("SELECT * FROM missing;", true)))
                .isInstanceOfSatisfying(ResponseStatusException.class, error -> {
                    assertThat(error.getStatusCode().value()).isEqualTo(400);
                    assertThat(error.getReason()).doesNotContain("private database details");
                });
        verify(connection, atLeastOnce()).rollback();
        verify(connection).close();
    }
}
