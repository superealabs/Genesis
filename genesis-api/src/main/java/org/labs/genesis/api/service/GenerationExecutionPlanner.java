package org.labs.genesis.api.service;

import org.labs.genesis.api.dto.GenerationPlanDtos.*;
import org.labs.genesis.api.dto.GenerationExecutionPlanDtos.*;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GenerationExecutionPlanner {

    private final GenerationProfilePlanner profilePlanner;

    public GenerationExecutionPlanner(GenerationProfilePlanner profilePlanner) {
        this.profilePlanner = profilePlanner;
    }

    public ExecutionPlanResponse prepare(List<GenerationParameterRequest> profiles) {
        GenerationPlanResponse plan = profilePlanner.plan(profiles);
        Map<BatchKey, List<String>> grouped = new LinkedHashMap<>();
        for (GenerationTask task : plan.tasks()) {
            BatchKey key = new BatchKey(task.profileName(), task.entityName(), task.view());
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(task.component());
        }
        List<ExecutionBatch> batches = new ArrayList<>();
        for (Map.Entry<BatchKey, List<String>> entry : grouped.entrySet()) {
            BatchKey key = entry.getKey();
            batches.add(new ExecutionBatch(key.profileName(), key.entityName(), key.view(), List.copyOf(entry.getValue())));
        }
        return new ExecutionPlanResponse(plan.profileCount(), plan.taskCount(), batches.size(), plan.skippedCount(), List.copyOf(batches));
    }

    private record BatchKey(
            String profileName,
            String entityName,
            boolean view
    ) {
    }
}
