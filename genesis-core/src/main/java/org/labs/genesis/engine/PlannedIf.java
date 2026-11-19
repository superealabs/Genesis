package org.labs.genesis.engine;

import java.util.List;

/** Regroupe les branches ordonnées d’un bloc conditionnel. */
record PlannedIf(List<PlannedBranch> branches) implements PlannedNode {}
