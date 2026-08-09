package org.labs.genesis.forms;

import lombok.Getter;

import javax.swing.*;

@Getter
public class FirstForm {
    private JPanel mainPanel;
    private JRadioButton addRuleToCode ;
    private JRadioButton syncProject;
    private JRadioButton generateNewProject;
    private JLabel generationLabel;
    private JLabel rulesLabel;
    private final ButtonGroup buttonGroup;

    public FirstForm() {
        buttonGroup = new ButtonGroup();
        buttonGroup.add(generateNewProject);
        buttonGroup.add(addRuleToCode);
        buttonGroup.add(syncProject);
        generateNewProject.setSelected(true);
    }
    public boolean generateNewProjectSelected() {return generateNewProject.isSelected();}
    public boolean ruleTodCodeSelected() {
        return addRuleToCode.isSelected();
    }
    public boolean syncSelected() {
        return syncProject.isSelected();
    }

}