package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Objects;

public record GenerationConfigurationSnapshot(
        List<GenerationParameterRequest> profiles,
        Map<String, RelationSelection> relationsByProfile
) {

    public GenerationConfigurationSnapshot {
        Objects.requireNonNull(profiles, "Profils obligatoires");
        Objects.requireNonNull(relationsByProfile, "Relations obligatoires");
        profiles = profiles.stream()
                .map(profile -> new GenerationParameterRequest(
                        profile.profileName(),
                        List.copyOf(profile.selectedTables()),
                        List.copyOf(profile.selectedViews()),
                        List.copyOf(profile.selectedComponents())
                ))
                .toList();

        Map<String, RelationSelection> copy = new LinkedHashMap<>();

        for (var entry : relationsByProfile.entrySet()) {
            copy.put(entry.getKey(),
                    new RelationSelection(entry.getValue().manuallyConfigured(), entry.getValue().relations())
            );
        }
        relationsByProfile = Map.copyOf(copy);
    }

    public record RelationSelection(
            boolean manuallyConfigured,
            List<RelationValue> relations
    ) {
        public RelationSelection {
            relations = List.copyOf(relations);
        }
    }

    public record RelationValue(
            String parentTable,
            String childTable,
            Boolean mandatory,
            Boolean hasForm
    ) {
    }

    public RelationSelection relationsFor(String profileName) {
        RelationSelection selection = relationsByProfile.get(profileName);

        if (selection == null) {
            throw new IllegalArgumentException("Profil absent de l'instantané : " + profileName
            );
        }

        return selection;
    }
}
