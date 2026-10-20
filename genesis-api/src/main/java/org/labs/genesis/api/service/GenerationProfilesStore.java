
package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.List;

@Component
public class GenerationProfilesStore {

    private List<GenerationParameterRequest> profiles = List.of();

    private Connection sourceConnection;
    private Object sourceFramework;
    private Object sourceLanguage;

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
        sourceFramework = context.getFramework();
        sourceLanguage = context.getLanguage();
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
        sourceFramework = null;
        sourceLanguage = null;
    }

    private boolean belongsTo(ProjectGenerationContext context) {
        return sourceConnection == context.getConnection() && sourceFramework == context.getFramework() && sourceLanguage == context.getLanguage();
    }
}
