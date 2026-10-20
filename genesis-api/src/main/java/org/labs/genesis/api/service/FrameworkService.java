package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.FrameworkDtos.*;
import org.labs.genesis.api.exception.FrameworkNotFoundException;
import org.labs.genesis.api.mapper.FrameworkMapper;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.ViewsTemplate;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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

    public List<LanguageResponse> getLanguages() {
        return ProjectGenerator.languages.values()
                .stream()
                .sorted(Comparator.comparingInt(Language::getId))
                .map(language -> new LanguageResponse(
                        language.getId(),
                        language.getName(),
                        language.getExtension()
                ))
                .toList();
    }

    public List<CoreFrameworkResponse> getCoreFrameworks() {
        List<String> names = ProjectGenerator.frameworks.values()
                .stream()
                .map(Framework::getCoreFramework)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        return IntStream.range(0, names.size())
                .mapToObj(index -> new CoreFrameworkResponse(
                        index + 1,
                        names.get(index)
                ))
                .toList();
    }

    public List<ViewTemplateResponse> getViewTemplates() {
        return ProjectGenerator.frameworks.values()
                .stream()
                .filter(FrameworkMVC.class::isInstance)
                .map(FrameworkMVC.class::cast)
                .map(FrameworkMVC::getViewsTemplate)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        ViewsTemplate::getId,
                        template -> template,
                        (first, second) -> first
                ))
                .values()
                .stream()
                .sorted(Comparator.comparingInt(ViewsTemplate::getId))
                .map(template -> new ViewTemplateResponse(
                        template.getId(),
                        template.getName()
                ))
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