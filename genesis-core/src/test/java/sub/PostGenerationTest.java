package sub;

import org.junit.jupiter.api.Test;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.sync.SyncGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostGenerationTest {
    private static final Logger log = LoggerFactory.getLogger(PostGenerationTest.class);

    @Test
    public void fetchContextFile() {
        String projectDirectory = "/home/itu-chan-alex/Stage/generated/ProjectBackup";
        SyncGenerator syncGenerator = new SyncGenerator();
        try {
            ProjectGenerationContext context = syncGenerator.loadProjectContext(projectDirectory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void evaluateContextFile() {
        String projectDirectory = "/home/itu-chan-alex/Stage/generated/ProjectBackup";
        SyncGenerator syncGenerator = new SyncGenerator();
        try {
            ProjectGenerationContext context = syncGenerator.loadProjectContext(projectDirectory);
            syncGenerator.evaluateDatabaseChanges(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void updateProject() {
        String projectDirectory = "/home/itu-chan-alex/Stage/generated/ProjectBackup";
        SyncGenerator syncGenerator = new SyncGenerator();
        try {
            ProjectGenerationContext context = syncGenerator.loadProjectContext(projectDirectory);
            syncGenerator.evaluateDatabaseChanges(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
