package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.framework.FrameworkResponse;
import org.labs.genesis.api.dto.framework.FrameworkSelectionResponse;
import org.labs.genesis.api.exception.FrameworkNotFoundException;
import org.labs.genesis.api.mapper.FrameworkMapper;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FrameworkService {

    private final GenerationContextStore generationContextStore;

    public FrameworkService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    public List<FrameworkResponse> getAll() {
        return ProjectGenerator.frameworks.values().stream()
                .sorted(Comparator.comparingInt(Framework::getId))
                .map(FrameworkMapper::toResponse)
                .toList();
    }

    public FrameworkSelectionResponse select(int id) {
        Framework framework = ProjectGenerator.frameworks.get(id);

        if (framework == null) {
            throw new FrameworkNotFoundException(id);
        }

        Language language = ProjectGenerator.findLanguageById(
                framework.getLanguageId()
        );

        generationContextStore.getContext()
                .setFramework(framework)
                .setLanguage(language);

        return new FrameworkSelectionResponse(true, FrameworkMapper.toResponse(framework));
    }
}