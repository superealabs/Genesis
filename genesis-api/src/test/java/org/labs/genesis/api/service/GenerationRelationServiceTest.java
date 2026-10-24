package org.labs.genesis.api.service;

import org.junit.jupiter.api.Test;
import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GenerationRelationServiceTest {
    private final GenerationRelationService service = new GenerationRelationService(mock(ProfileRelationService.class));

    @Test
    void rejectsContradictoryFlagsAcrossProfilesAndAcceptsAnExplicitEmptySelection() {
        var relations = new LinkedHashMap<String, List<RelationParameter>>();
        relations.put("A", List.of(new RelationParameter(" Parent ", "Child", true, false)));
        relations.put("B", List.of(new RelationParameter("parent", "child", false, false)));
        assertThatThrownBy(() -> service.validateRelations(relations)).isInstanceOf(RelationException.class)
                .hasMessageContaining("entre les profils 'A' et 'B'").hasMessageContaining("incompatibles");
        relations.put("B", List.of(new RelationParameter("parent", "child", true, false)));
        assertThatCode(() -> service.validateRelations(relations)).doesNotThrowAnyException();
        assertThatCode(() -> service.validateRelations(Map.of("Empty", List.of()))).doesNotThrowAnyException();
    }

    @Test
    void keepsUnconfiguredForeignKeysDistinctFromConfiguredRelations() {
        var parent = table("parent"); var child = table("child");
        var foreign = new ColumnMetadata(); foreign.setForeign(true); foreign.setReferencedTable("parent");
        child.setColumns(new ColumnMetadata[]{foreign});
        var context = new ProjectGenerationContext(); context.setEntityTables(List.of(parent, child));
        context.setRelationParameters(List.of());
        var empty = service.inspect(context);
        assertThat(empty.configuredRelations()).isEmpty();
        assertThat(empty.unconfiguredForeignKeys()).containsExactly(new GenerationRelationService.RelationCandidate("parent", "child"));
        context.setRelationParameters(List.of(new RelationParameter("parent", "child", true, true)));
        var configured = service.inspect(context);
        assertThat(configured.configuredRelations()).hasSize(1);
        assertThat(configured.unconfiguredForeignKeys()).isEmpty();
    }

    @Test
    void retainsTheOriginalErrorMessagesOfBothValidationPaths() {
        assertThatThrownBy(() -> service.validateRelations(Map.of("A", List.of(new RelationParameter("", "child", true, false)))))
                .isInstanceOf(RelationException.class).hasMessage("Nom de table invalide dans une relation");
        var context = new ProjectGenerationContext(); context.setEntityTables(List.of());
        context.setRelationParameters(List.of(new RelationParameter("", "child", true, false)));
        assertThatThrownBy(() -> service.inspect(context)).isInstanceOf(RelationException.class).hasMessage("Nom de table invalide");
    }

    private TableMetadata table(String name) {
        var table = new TableMetadata(); table.setTableName(name); table.setClassName(name);
        table.setIsView(false); table.setColumns(new ColumnMetadata[0]); return table;
    }
}
