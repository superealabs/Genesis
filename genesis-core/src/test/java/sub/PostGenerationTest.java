package sub;

import org.junit.jupiter.api.Test;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.config.langage.generator.sync.EvaluationParameters;
import org.labs.genesis.config.langage.generator.sync.SyncGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostGenerationTest {
    private static final Logger log = LoggerFactory.getLogger(PostGenerationTest.class);

    @Test
    public void fetchContextFile() {
        String projectDirectory = "/home/itu-chan-alex/Stage/generated/FanambyPresence";
        ProjectGenerator projectGenerator = new ProjectGenerator();
        SyncGenerator syncGenerator = new SyncGenerator();
        EvaluationParameters evaluationParameters = new EvaluationParameters();
        try {
            ProjectGenerationContext context = syncGenerator.loadProjectContext(projectDirectory);
            syncGenerator.evaluateDatabaseChanges(context, evaluationParameters);
            syncGenerator.generateProject(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
