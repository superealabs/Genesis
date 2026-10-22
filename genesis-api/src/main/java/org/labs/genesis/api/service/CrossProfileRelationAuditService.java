package org.labs.genesis.api.service;

import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CrossProfileRelationAuditService {
    public RelationAuditReport inspect(ProjectGenerationContext context) {
        if (context == null || context.getEntityTables() == null) {
            throw new RelationException("Métadonnées des tables indisponibles");
        }
        Map<RelationKey, RelationCandidate> detected = new LinkedHashMap<>();
        Set<RelationKey> configured = new HashSet<>();
        List<RelationParameter> relations = context.getRelationParameters() == null
                        ? List.of()
                        : context.getRelationParameters();

        for (RelationParameter relation : relations) {
            if (relation == null) {
                throw new RelationException("Relation configurée invalide");
            }
            configured.add(new RelationKey(normalize(relation.getParentTable()), normalize(relation.getChildTable())));
        }

        for (TableMetadata child : context.getEntityTables()) {
            if (child == null || child.getColumns() == null) {
                continue;
            }

            for (ColumnMetadata column : child.getColumns()) {
                if (column == null
                        || !column.isForeign()
                        || column.getReferencedTable() == null) {
                    continue;
                }

                TableMetadata parent = context.findTableByName(column.getReferencedTable(), context.getEntityTables());

                if (parent == null || parent.getTableName().equalsIgnoreCase(child.getTableName())) {
                    continue;
                }

                RelationKey key = new RelationKey(normalize(parent.getTableName()), normalize(child.getTableName()));

                detected.putIfAbsent(key, new RelationCandidate(parent.getTableName(), child.getTableName()));
            }
        }

        List<RelationCandidate> active = new ArrayList<>();
        List<RelationCandidate> unconfigured = new ArrayList<>();

        for (var entry : detected.entrySet()) {
            if (configured.contains(entry.getKey())) {
                active.add(entry.getValue());
            } else {
                unconfigured.add(entry.getValue());
            }
        }

        for (RelationKey key : configured) {
            if (!detected.containsKey(key)) {
                throw new RelationException("La relation configurée " + key.parent() + " -> " + key.child() + " n'existe pas dans les métadonnées");
            }
        }

        return new RelationAuditReport(List.copyOf(active), List.copyOf(unconfigured));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new RelationException("Nom de table invalide");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record RelationKey(
            String parent,
            String child
    ) {
    }

    public record RelationCandidate(
            String parentTable,
            String childTable
    ) {
    }

    public record RelationAuditReport(
            List<RelationCandidate> configuredRelations,
            List<RelationCandidate> unconfiguredForeignKeys
    ) {
    }
}
