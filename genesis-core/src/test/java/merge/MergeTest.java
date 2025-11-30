package merge;

import org.junit.jupiter.api.Test;
import org.labs.genesis.merge.FileMergeInput;
import org.labs.genesis.merge.MergeOutcome;
import org.labs.genesis.merge.MergeTool;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MergeTest {

    @Test
    public void testMerge() throws IOException {
        File base = new File("/home/itu-chan-alex/Stage/generated/FanambyPresenceBackup/FanambyPresence/src/main/java/mg/akademia/fanambypresence/services/DefaultMatchFootService.java");
        File user = new File("/home/itu-chan-alex/Stage/generated/FanambyPresenceBackup/FanambyPresence/src/main/java/mg/akademia/fanambypresence/services/DefaultMatchFootService.java");
        File theirs = new File("/home/itu-chan-alex/Stage/generated/FanambyPresenceCopy/FanambyPresenceCopy/src/main/java/mg/akademia/fanambypresencecopy/services/DefaultMatchFootService.java");

        FileMergeInput mergeInput = new FileMergeInput(base, user, theirs);
        MergeOutcome merged = MergeTool.merge(mergeInput, true);

//        if (!merged.contains("<<<<<<<")) {
//            Files.write(user.toPath(), merged.getBytes(StandardCharsets.UTF_8));
//        } else {
            Files.write(new File("/home/itu-chan-alex/Stage/generated/.conflicts/Test.conflict").toPath(), merged.mergedContent.getBytes(StandardCharsets.UTF_8));
//        }

    }
}
