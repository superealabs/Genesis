package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.exception.RelationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class GenerationRelationConflictValidator {

    private final ProfileRelationService profileRelationService;

    public GenerationRelationConflictValidator(ProfileRelationService profileRelationService) {
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
                    throw new RelationException(
                            "Conflit de relation "
                                    + relation.getParentTable()
                                    + " -> "
                                    + relation.getChildTable()
                                    + " entre les profils '"
                                    + previous.profileName()
                                    + "' et '"
                                    + profile.profileName()
                                    + "'. Valeurs mandatory ou hasForm "
                                    + "incompatibles."
                    );
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
                    throw new RelationException(
                            "Conflit de relation "
                                    + relation.getParentTable()
                                    + " -> "
                                    + relation.getChildTable()
                                    + " entre les profils '"
                                    + previous.profileName()
                                    + "' et '"
                                    + profileName
                                    + "'. Valeurs mandatory ou hasForm incompatibles."
                    );
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
}
