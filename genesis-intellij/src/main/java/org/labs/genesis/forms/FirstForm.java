package org.labs.genesis.forms;

import lombok.Getter;

import javax.swing.*;

@Getter
public class FirstForm {
    private JPanel mainPanel;
    private JRadioButton createProject ;
    private JRadioButton addRuleToCode ;
    private ButtonGroup buttonGroup;

    public FirstForm() {
        buttonGroup = new ButtonGroup();
        buttonGroup.add(createProject);
        buttonGroup.add(addRuleToCode);
    }
    public boolean ruleTodCodeSelected() {
        return addRuleToCode.isSelected();
    }
}