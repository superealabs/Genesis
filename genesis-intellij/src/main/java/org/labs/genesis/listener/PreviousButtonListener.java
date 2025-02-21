package org.labs.genesis.listener;

import org.labs.genesis.forms.GenerationOptionForm;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PreviousButtonListener extends MouseAdapter {
    private final GenerationOptionForm generationOptionForm;

    public PreviousButtonListener(GenerationOptionForm generationOptionForm) {
        this.generationOptionForm = generationOptionForm;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int index = generationOptionForm.getPaginationIndex();
        if (index == 0) {
            return;
        }
        index--;
        generationOptionForm.setPaginationIndex(index);
        generationOptionForm.populateTableNames();
    }
}
