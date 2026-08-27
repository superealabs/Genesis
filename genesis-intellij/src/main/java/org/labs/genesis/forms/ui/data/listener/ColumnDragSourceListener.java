package org.labs.genesis.forms.ui.data.listener;

import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;

public class ColumnDragSourceListener extends DragSourceAdapter {
    @Override
    public void dragEnter(DragSourceDragEvent event) {
        // Rien à faire
    }

    @Override
    public void dragExit(DragSourceEvent event) {
        // Rien à faire
    }

    @Override
    public void dragOver(DragSourceDragEvent event) {
        // Rien à faire
    }

    @Override
    public void dropActionChanged(DragSourceDragEvent event) {
        // Rien à faire
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent event) {
        // Le drag est terminé
    }
}