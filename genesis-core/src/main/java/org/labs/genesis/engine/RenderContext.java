package org.labs.genesis.engine;

import java.util.Map;

/** Conserve les analyses et conversions partagées pendant un seul rendu. */
final class RenderContext {
    Map<String, ConditionalPlan> conditions;
    Map<String, Object> literals;
}
