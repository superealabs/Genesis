package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.FrameworkResponse;
import org.labs.genesis.api.mapper.FrameworkMapper;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FrameworkService {

    public List<FrameworkResponse> getAll() {
        return ProjectGenerator.frameworks.values().stream()
                .sorted(Comparator.comparingInt(framework -> framework.getId()))
                .map(FrameworkMapper::toResponse)
                .toList();
    }
}