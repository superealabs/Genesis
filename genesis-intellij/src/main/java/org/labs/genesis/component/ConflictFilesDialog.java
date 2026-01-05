package org.labs.genesis.component;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ConflictFilesDialog extends DialogWrapper {
    private final java.util.List<File> conflictFiles;
    private final int conflictCount;

    public ConflictFilesDialog(Project project, java.util.List<File> conflictFiles) {
        super(project);
        this.conflictFiles = conflictFiles;
        this.conflictCount = conflictFiles.size();
        setTitle("Synchronization Complete");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // Message d'en-tête
        JLabel headerLabel = new JLabel("Project synchronization completed successfully with "
                + conflictCount + " conflicts");
        panel.add(headerLabel, BorderLayout.NORTH);

        // Liste scrollable des fichiers
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (File f : conflictFiles) {
            listModel.addElement(f.getAbsolutePath());
        }

        JBList<String> fileList = new JBList<>(listModel);
        fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JBScrollPane scrollPane = new JBScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(500, 200));

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}