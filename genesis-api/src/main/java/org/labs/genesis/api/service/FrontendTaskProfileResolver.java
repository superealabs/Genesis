package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.exception.TableSelectionException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FrontendTaskProfileResolver {
    public String resolve(List<GenerationParameterRequest> profiles, String entityName, boolean view) {
        if (profiles == null || profiles.isEmpty()) {
            throw new TableSelectionException("Aucun profil disponible");
        }

        for (GenerationParameterRequest profile : profiles) {
            if (profile == null) {
                continue;
            }

            List<String> names = view ? profile.selectedViews() : profile.selectedTables();

            if (names != null && names.contains(entityName)) {
                return profile.profileName();
            }
        }

        throw new TableSelectionException("Aucun profil ne contient l'entité : " + entityName);
    }
}
