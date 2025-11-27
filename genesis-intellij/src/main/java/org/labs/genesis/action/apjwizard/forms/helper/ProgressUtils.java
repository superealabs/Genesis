package org.labs.genesis.action.apjwizard.forms.helper;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;

import java.util.concurrent.atomic.AtomicReference;
public final class ProgressUtils {

    public static void checkCanceled(ProgressIndicator indicator) {
        if (indicator != null && indicator.isCanceled()) {
            throw new ProcessCanceledException();
        }
    }

    public static void updateProgress(ProgressIndicator indicator, String text, double fraction) {
        checkCanceled(indicator);
        if (indicator != null) {
            indicator.setText(text);
            indicator.setFraction(fraction);
        }
    }

    public static void runWithProgress(Project project, String title, ProgressTask task) throws ConfigurationException {
        AtomicReference<ConfigurationException> exceptionRef = new AtomicReference<>();
        boolean success = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try {
                task.run(ProgressManager.getInstance().getProgressIndicator());
            } catch (ConfigurationException e) {
                exceptionRef.set(e);
            }
        }, title, true, project);

        if (!success) throw new ConfigurationException("Operation cancelled by user.");
        if (exceptionRef.get() != null) throw exceptionRef.get();
    }

    @FunctionalInterface
    public interface ProgressTask {
        void run(ProgressIndicator indicator) throws ConfigurationException, ProcessCanceledException;
    }
}
