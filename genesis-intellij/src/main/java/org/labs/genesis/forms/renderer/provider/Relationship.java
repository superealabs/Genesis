package org.labs.genesis.forms.renderer.provider;

public record Relationship(
        String leftTable,
        String leftColumn,
        String rightTable,
        String rightColumn,
        JoinType joinType,
        String cardinality
) {
    public enum JoinType { INNER, LEFT }

    public Relationship {
        if (leftTable == null || leftColumn == null || rightTable == null || rightColumn == null) {
            throw new IllegalArgumentException("Relationship is incomplete");
        }
        joinType = joinType == null ? JoinType.LEFT : joinType;
    }
}
