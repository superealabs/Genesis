package org.labs.genesis.action.apjwizard.forms.helper;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;

import java.util.concurrent.atomic.AtomicReference;
public final class ProgressUtils {

    public static void checkCanceled(ProgressIndicator indicator) throws ProcessCanceledException {
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
        AtomicReference<Exception> exceptionRef = new AtomicReference<>();
        boolean success = ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            try {
                ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
                task.run(indicator);
            } catch (Exception e) {
                exceptionRef.set(e);
            }
        }, title, true, project);

        if (!success) {
            throw new ConfigurationException("Opération annulée par l’utilisateur.");
        }

        if (exceptionRef.get() != null) {
            Exception e = exceptionRef.get();
            if (e instanceof ProcessCanceledException) {
                throw new ConfigurationException("Opération annulée par l’utilisateur.");
            }
            if (e instanceof ConfigurationException) throw (ConfigurationException) e;
            throw new ConfigurationException("Erreur : " + e.getMessage());
        }
    }

    @FunctionalInterface
    public interface ProgressTask {
        void run(ProgressIndicator indicator) throws Exception;
    }
}
