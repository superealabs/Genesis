package org.labs.genesis.engine;

/** Décrit une boucle et sa position dans le template. */
record LoopInfo(String loopVarName, String loopContent, int start) {}
