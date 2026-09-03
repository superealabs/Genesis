package org.labs.genesis.api.dto.relation;

public record RelationResponse(
        String parentTable,
        String childTable,
        boolean mandatory,
        boolean hasForm
) {
}
