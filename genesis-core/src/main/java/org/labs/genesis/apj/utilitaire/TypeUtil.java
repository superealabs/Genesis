package org.labs.genesis.apj.utilitaire;

import java.util.Set;
import java.util.stream.Collectors;

public class TypeUtil {

    private static final Set<String> RANGEABLE_TYPES = Set.of(
            "int", "integer", "long", "double", "float",
            "LocalDate", "LocalDateTime", "Date"
        ).stream()
        .map(String::toLowerCase)
        .collect(Collectors.toSet());

    private static final Set<String> SUMMABLE_TYPES = Set.of(
        "int", "integer", "Long", "double","float"
    );

    public static boolean isSummable(String typeName) {
        if (typeName == null) return false;
        return SUMMABLE_TYPES.contains(typeName.trim().toLowerCase());
    }

    public static boolean isRangeable(String typeName) {
        if (typeName == null) return false;
        return RANGEABLE_TYPES.contains(typeName.trim().toLowerCase());
    }
}
