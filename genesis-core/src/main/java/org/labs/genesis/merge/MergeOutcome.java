package org.labs.genesis.merge;

import java.io.File;

public class MergeOutcome {
    public final FileMergeInput input;
    public final boolean hasConflict;
    public final String mergedContent;
    public final File conflictFile;

    public MergeOutcome(FileMergeInput inputs, boolean hasConflict, String mergedContent, File conflictFile) {
        this.hasConflict = hasConflict;
        this.mergedContent = mergedContent;
        this.conflictFile = conflictFile;
        this.input = inputs;
    }
}
