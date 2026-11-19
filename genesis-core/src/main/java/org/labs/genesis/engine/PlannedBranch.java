package org.labs.genesis.engine;

/** Associe une condition à son corps ; null représente la branche else. */
record PlannedBranch(String condition, ConditionalPlan body) {}
