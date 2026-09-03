package org.labs.genesis.api.dto.relation;

import java.util.List;

public record RelationUpdateResponse(
        boolean success,
        List<RelationResponse> relations
) {
}
