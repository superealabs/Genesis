package org.labs.genesis.merge;

import lombok.AllArgsConstructor;

import java.io.File;

@AllArgsConstructor
public class FileMergeInput {
    public final File baseFile;
    public final File currentFile;
    public final File newFile;
}
