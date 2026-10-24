package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

class ApiConfigurationTest {
    @Test
    void sqlExecutionFlagUsesTheDocumentedEnvironmentVariable() throws Exception {
        var sources = new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"));
        assertThat(sources.getFirst().getProperty("genesis.sql.execution-enabled"))
                .isEqualTo("${GENESIS_SQL_EXECUTION_ENABLED:false}");
    }
}
