package org.labs.genesis.api.mapper;

import org.labs.genesis.api.dto.relation.RelationResponse;
import org.labs.genesis.connexion.model.RelationParameter;

public final class RelationMapper {

    private RelationMapper() {
    }

    public static RelationResponse toResponse(RelationParameter relation) {
        return new RelationResponse(
                relation.getParentTable(),
                relation.getChildTable(),
                Boolean.TRUE.equals(relation.getMandatory()),
                Boolean.TRUE.equals(relation.getHasForm())
        );
    }
}
