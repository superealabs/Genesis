package org.labs.genesis.api.dto.relation;

public record RelationUpdateRequest(
        String parentTable,
        String childTable,
        boolean mandatory,
        boolean hasForm
) {
}
