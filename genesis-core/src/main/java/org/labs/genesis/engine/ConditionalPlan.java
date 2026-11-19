package org.labs.genesis.engine;

import java.util.List;

/** Décrit les éléments conditionnels analysés pour un fragment de template. */
record ConditionalPlan(List<PlannedNode> nodes) {}
