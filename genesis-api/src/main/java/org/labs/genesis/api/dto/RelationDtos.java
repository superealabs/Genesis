package org.labs.genesis.api.dto;

import java.util.List;

public final class RelationDtos {

    private RelationDtos() {
    }

    public record RelationResponse(
            String parentTable,
            String childTable,
            boolean mandatory,
            boolean hasForm
    ) {
    }

    public record RelationUpdateRequest(
            String parentTable,
            String childTable,
            boolean mandatory,
            boolean hasForm
    ) {
    }

    public record RelationUpdateResponse(
            boolean success,
            List<RelationResponse> relations
    ) {
    }

    // Requête attendue par Genesis SDK Web
    public record RelationParametersRequest(
            List<RelationUpdateRequest> relations
    ) {
    }

    // Réponse attendue par Genesis SDK Web
    public record RelationParametersResponse(
            boolean success,
            String message
    ) {
    }
}
