
package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

@Component
public class GenerationProfilesStore {

    private List<GenerationParameterRequest> profiles = List.of();

    private Connection sourceConnection;
    private Integer sourceFrameworkId;
    private Integer sourceLanguageId;

    public synchronized void save(ProjectGenerationContext context, List<GenerationParameterRequest> parameters) {
        List<GenerationParameterRequest> snapshot = parameters.stream()
                        .map(profile -> new GenerationParameterRequest(
                                profile.profileName(),
                                List.copyOf(profile.selectedTables()),
                                List.copyOf(profile.selectedViews()),
                                List.copyOf(profile.selectedComponents())
                        ))
                        .toList();

        profiles = snapshot;
        sourceConnection = context.getConnection();
        sourceFrameworkId = context.getFramework() == null ? null : context.getFramework().getId();
        sourceLanguageId = context.getLanguage() == null ? null : context.getLanguage().getId();
    }

    public synchronized List<GenerationParameterRequest> getProfiles(ProjectGenerationContext context) {
        if (!belongsTo(context)) {
            return List.of();
        }

        return profiles;
    }

    public synchronized void clear() {
        profiles = List.of();
        sourceConnection = null;
        sourceFrameworkId = null;
        sourceLanguageId = null;
    }

    private boolean belongsTo(ProjectGenerationContext context) {
        Integer frameworkId = context.getFramework() == null ? null : context.getFramework().getId();
        Integer languageId = context.getLanguage() == null ? null : context.getLanguage().getId();
        return sourceConnection == context.getConnection()
                && Objects.equals(sourceFrameworkId, frameworkId)
                && Objects.equals(sourceLanguageId, languageId);
    }
}
