package org.labs.genesis.merge;

import org.eclipse.jgit.diff.HistogramDiff;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.merge.MergeAlgorithm;
import org.eclipse.jgit.merge.MergeFormatter;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.merge.MergeStrategy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MergeTool {
    public static MergeOutcome merge(
            FileMergeInput input,
            boolean ignoreWhitespace
    ) throws IOException {
        String mergedContent = null;
        if (input.baseFile == null || !input.baseFile.exists()) {
            byte[] freshContent = Files.readAllBytes(input.newFile.toPath());
            Files.write(input.currentFile.toPath(), freshContent);
            if (input.baseFile != null) {
                Files.write(input.baseFile.toPath(), freshContent);
            }
            mergedContent = new String(freshContent, StandardCharsets.UTF_8);
            return new MergeOutcome(input,false, mergedContent, null);
        }
        if (!input.currentFile.exists()) {
            return new MergeOutcome(input,false, null, null);
        }
        RawText base   = new RawText(Files.readAllBytes(input.baseFile.toPath()));
        RawText ours   = new RawText(Files.readAllBytes(input.currentFile.toPath()));
        RawText theirs = new RawText(Files.readAllBytes(input.newFile.toPath()));

        RawTextComparator comparator = ignoreWhitespace
                ? RawTextComparator.WS_IGNORE_ALL
                : RawTextComparator.DEFAULT;

        MergeAlgorithm algo = new MergeAlgorithm();
        MergeResult<RawText> result = algo.merge(
                comparator,
                base, ours, theirs
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MergeFormatter().formatMerge(
                out,
                result,
                "BASE", "CURRENT", "GENERATED",
                StandardCharsets.UTF_8.name()
        );
        mergedContent = out.toString(StandardCharsets.UTF_8);
        boolean hasConflict = mergedContent.contains("<<<<<<<");
        if (!hasConflict) {
            return new MergeOutcome(input,false, mergedContent, null);
        }
        File conflictFile = new File(input.currentFile.getPath());
        return  new MergeOutcome(input, true, mergedContent, conflictFile);
    }
}
