package org.labs.genesis.api.service;

import java.util.*;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.springframework.stereotype.Service;

@Service
public class GenerationRelationService {

    private final ProfileRelationService profileRelationService;

    public GenerationRelationService(ProfileRelationService profileRelationService) {
        this.profileRelationService = profileRelationService;
    }

    public void validate(List<GenerationParameterRequest> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            throw new RelationException("Aucun profil de génération disponible");
        }
        Map<RelationKey, OwnedRelation> encountered = new HashMap<>();
        for (GenerationParameterRequest profile : profiles) {
            ProjectGenerationContext context = profileRelationService.buildContext(profile.profileName());
            List<RelationParameter> relations = context.getRelationParameters();
            if (relations == null) {
                continue;
            }
            for (RelationParameter relation : relations) {
                RelationKey key = new RelationKey(normalize(relation.getParentTable()), normalize(relation.getChildTable()));
                OwnedRelation previous = encountered.get(key);
                if (previous == null) {
                    encountered.put(key, new OwnedRelation(profile.profileName(), relation.getMandatory(), relation.getHasForm()));
                    continue;
                }
                boolean sameMandatory = Objects.equals(previous.mandatory(), relation.getMandatory());
                boolean sameHasForm = Objects.equals(previous.hasForm(), relation.getHasForm());
                if (!sameMandatory || !sameHasForm) {
                    throw new RelationException("Conflit de relation " + relation.getParentTable() + " -> " + relation.getChildTable() + " entre les profils '" + previous.profileName() + "' et '" + profile.profileName() + "'. Valeurs mandatory ou hasForm " + "incompatibles.");
                }
            }
        }
    }

    public void validateRelations(Map<String, List<RelationParameter>> relationsByProfile) {
        if (relationsByProfile == null || relationsByProfile.isEmpty()) {
            throw new RelationException("Aucune configuration de relations à valider");
        }
        Map<RelationKey, OwnedRelation> encountered = new HashMap<>();
        for (var entry : relationsByProfile.entrySet()) {
            String profileName = entry.getKey();
            List<RelationParameter> relations = entry.getValue();
            if (profileName == null || profileName.isBlank() || relations == null) {
                throw new RelationException("Configuration de relations invalide");
            }
            for (RelationParameter relation : relations) {
                if (relation == null) {
                    throw new RelationException("Relation nulle dans le profil " + profileName);
                }
                RelationKey key = new RelationKey(normalize(relation.getParentTable()), normalize(relation.getChildTable()));
                OwnedRelation previous = encountered.get(key);
                if (previous == null) {
                    encountered.put(key, new OwnedRelation(profileName, relation.getMandatory(), relation.getHasForm()));
                    continue;
                }
                if (!Objects.equals(previous.mandatory(), relation.getMandatory()) || !Objects.equals(previous.hasForm(), relation.getHasForm())) {
                    throw new RelationException("Conflit de relation " + relation.getParentTable() + " -> " + relation.getChildTable() + " entre les profils '" + previous.profileName() + "' et '" + profileName + "'. Valeurs mandatory ou hasForm incompatibles.");
                }
            }
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new RelationException("Nom de table invalide dans une relation");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private record RelationKey(String parent, String child) {
    }

    private record OwnedRelation(String profileName, Boolean mandatory, Boolean hasForm) {
    }

    public RelationAuditReport inspect(ProjectGenerationContext context) {
        if (context == null || context.getEntityTables() == null) {
            throw new RelationException("Métadonnées des tables indisponibles");
        }
        Map<RelationKey, RelationCandidate> detected = new LinkedHashMap<>();
        Set<RelationKey> configured = new HashSet<>();
        List<RelationParameter> relations = context.getRelationParameters() == null ? List.of() : context.getRelationParameters();
        for (RelationParameter relation : relations) {
            if (relation == null) {
                throw new RelationException("Relation configurée invalide");
            }
            configured.add(new RelationKey(normalizeAuditTable(relation.getParentTable()), normalizeAuditTable(relation.getChildTable())));
        }
        for (TableMetadata child : context.getEntityTables()) {
            if (child == null || child.getColumns() == null) {
                continue;
            }
            for (ColumnMetadata column : child.getColumns()) {
                if (column == null || !column.isForeign() || column.getReferencedTable() == null) {
                    continue;
                }
                TableMetadata parent = context.findTableByName(column.getReferencedTable(), context.getEntityTables());
                if (parent == null || parent.getTableName().equalsIgnoreCase(child.getTableName())) {
                    continue;
                }
                RelationKey key = new RelationKey(normalizeAuditTable(parent.getTableName()), normalizeAuditTable(child.getTableName()));
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

    private String normalizeAuditTable(String value) {
        if (value == null || value.isBlank()) {
            throw new RelationException("Nom de table invalide");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public record RelationCandidate(String parentTable, String childTable) {
    }

    public record RelationAuditReport(List<RelationCandidate> configuredRelations, List<RelationCandidate> unconfiguredForeignKeys) {
    }
}
