package org.labs.genesis.action.apjwizard.forms.popup;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public class TableChooserDialog extends DialogWrapper {
    private final Tree tree;

    public TableChooserDialog(Component parent, Tree tree) {
        super(parent, true);
        this.tree = tree;
        setTitle("Choisir une table ou une vue");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JBScrollPane scroll = new JBScrollPane(tree);
        scroll.setPreferredSize(new Dimension(450, 300));
        return scroll;
    }

    public TreePath getSelection() {
        return tree.getSelectionPath();
    }

    @Override
    protected Action @NotNull [] createActions() {
        Action ok = getOKAction();
        Action cancel = getCancelAction();
        ok.putValue(DEFAULT_ACTION, null);
        cancel.putValue(DEFAULT_ACTION, null);
        return new Action[]{ ok, cancel };
    }

    public void triggerOK() {
        doOKAction();
    }


}
