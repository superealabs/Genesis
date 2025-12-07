package org.labs.genesis.action.apjwizard;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.labs.genesis.action.apjwizard.forms.popup.PopUtils;
import org.labs.genesis.action.apjwizard.steps.*;
import org.labs.genesis.apj.ApjGenerationContext;
import org.labs.genesis.apj.utilitaire.ConstantesApj;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;


public class ApjWizardDialog extends DialogWrapper {
    private final JPanel mainPanel;
    private final ApjGenerationContext context;
    private final List<WizardStep> steps = new ArrayList<>();
    private int currentStepIndex = 0;
    private final Project project;

    public ApjWizardDialog(Project project, VirtualFile file) {
        super(true);
        context = new ApjGenerationContext();
        if (file != null) {
            String folderPath = file.isDirectory() ? file.getPath() : file.getParent().getPath();
            context.setLocationDir(folderPath);
        }
        mainPanel = new JPanel(new BorderLayout());
        steps.add(new PropertiesWizardStep(context,project));
        this.project = project;
        init();
        updateStep();
    }

    private void updateStep() {
        WizardStep step = steps.get(currentStepIndex);
        mainPanel.removeAll();
        mainPanel.add(step.getComponent(), BorderLayout.CENTER);
        setTitle(step.getTitle());
        mainPanel.revalidate();
        mainPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            pack();
            centerDialog();
        });
    }

    private void nextStep() throws ConfigurationException {
        WizardStep step = steps.get(currentStepIndex);

        if (!step.validateStep()) return;
        step.onNext();

        if (currentStepIndex == 0) {
            WizardStep nextStep = switch (context.getApjfile().getId()) {
                case ConstantesApj.PAGE_RECHERCHE_ID -> new PageRechercheWizardStep(context,project);
                case ConstantesApj.PAGE_INSERT_ID -> new PageInsertWizardStep(context,project);
                case ConstantesApj.PAGE_CONSULTE_ID -> new PageConsulteWizardStep(context,project);
                case ConstantesApj.PAGE_INSERT_MULTIPLE_ID -> new PageInsertMultipleWizardStep(context,project);
                default -> null;
            };

            if (nextStep != null) {
                if (steps.size() > 1) {
                    steps.set(1, nextStep);
                } else {
                    steps.add(nextStep);
                }
                currentStepIndex++;
                updateStep();
                return;
            }
        }

        if (currentStepIndex == steps.size() - 1) {
            close(OK_EXIT_CODE);
        }
    }


    private void previousStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--;
            updateStep();
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return mainPanel;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{
            new DialogWrapperAction("Précédent") {
                @Override
                protected void doAction(ActionEvent e) {
                    previousStep();
                }
            },
            new DialogWrapperAction("Suivant") {
                @Override
                protected void doAction(ActionEvent e) {
                    try {
                        nextStep();
                    } catch (ConfigurationException ex) {
                        PopUtils.showValidationError(mainPanel, ex);
                    }
                }
            },
            new DialogWrapperAction("Annuler") {
                @Override
                protected void doAction(ActionEvent e) {
                    doCancelAction();
                }
            }
        };
    }

    @Override
    protected void doOKAction() {
        WizardStep step = steps.get(currentStepIndex);
        try {
            if (!step.validateStep()) return;
        } catch (ConfigurationException e) {
            PopUtils.showValidationError(mainPanel,e);
            return;
        }

        try {
            step.onNext();
        } catch (ConfigurationException ex) {
            PopUtils.showValidationError(mainPanel,ex);
        }
        super.doOKAction();
    }

    private void centerDialog() {
        SwingUtilities.invokeLater(() -> {
            Window window = getWindow();
            if (window == null) return;

            GraphicsConfiguration gc = window.getGraphicsConfiguration();
            if (gc == null) {
                window.setLocationRelativeTo(null);
                return;
            }

            Rectangle bounds = gc.getBounds();
            int x = bounds.x + (bounds.width - window.getWidth()) / 2;
            int y = bounds.y + (bounds.height - window.getHeight()) / 2;
            window.setLocation(x, y);
        });
    }
}
