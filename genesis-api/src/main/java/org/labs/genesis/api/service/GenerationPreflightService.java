package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.GenerationExecutionPlanDtos.ExecutionPlanResponse;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class GenerationPreflightService {

    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final GenerationRelationConflictValidator conflictValidator;
    private final GenerationExecutionPlanner executionPlanner;

    private final GenerationExecutionContextFactory contextFactory;

    private final CrossProfileRelationAuditService relationAuditService;
    private final FrontendExecutionTaskPlanner frontendPlanner;

    public GenerationPreflightService(
            GenerationContextStore contextStore,
            GenerationProfilesStore profilesStore,
            GenerationRelationConflictValidator conflictValidator,
            GenerationExecutionPlanner executionPlanner,
            GenerationExecutionContextFactory contextFactory,
            CrossProfileRelationAuditService relationAuditService,
            FrontendExecutionTaskPlanner frontendPlanner
    ) {
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.conflictValidator = conflictValidator;
        this.executionPlanner = executionPlanner;
        this.contextFactory = contextFactory;
        this.relationAuditService = relationAuditService;
        this.frontendPlanner = frontendPlanner;
    }


    public PreflightResult prepare(GenerationConfigurationSnapshot snapshot) {
        ProjectGenerationContext source = contextStore.getContext();
        List<GenerationParameterRequest> profiles = snapshot.profiles();
        if (profiles == null || profiles.isEmpty()) {
            throw new org.labs.genesis.api.exception.ProjectConfigException("Aucun profil de génération disponible");
        }

        ExecutionPlanResponse executionPlan = executionPlanner.prepare(profiles);
        CrossProfileRelationAuditService.RelationAuditReport relationReport;
        List<FrontendExecutionTaskPlanner.FrontendExecutionTask> frontendTasks;

        // La construction du contexte consolidé assure
        // également la validation des relations.
        try (var execution = contextFactory.openConsolidated(snapshot)) {
            ProjectGenerationContext consolidated = execution.context();
            relationReport = relationAuditService.inspect(consolidated);
            frontendTasks = frontendPlanner.prepare(executionPlan.batches(), consolidated);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new PreflightResult(executionPlan, relationReport, List.copyOf(frontendTasks));
    }

    public record PreflightResult(
            ExecutionPlanResponse executionPlan,
            CrossProfileRelationAuditService.RelationAuditReport relations,
            List<FrontendExecutionTaskPlanner.FrontendExecutionTask> frontendTasks
    ) {
    }
}