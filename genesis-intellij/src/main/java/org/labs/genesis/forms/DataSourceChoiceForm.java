package org.labs.genesis.forms;

import lombok.Getter;

import javax.swing.*;

@Getter
public class DataSourceChoiceForm {

    private JPanel mainPanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JCheckBox databaseCheckBox;
    private JLabel databaseDescLabel;
    private JCheckBox scriptCheckBox;
    private JLabel scriptDescLabel;

    public DataSourceChoiceForm() {
        databaseCheckBox.setSelected(true);
        scriptCheckBox.setSelected(false);

        databaseCheckBox.addActionListener(e -> {
            databaseCheckBox.setSelected(true);
            scriptCheckBox.setSelected(false);
        });

        scriptCheckBox.addActionListener(e -> {
            scriptCheckBox.setSelected(true);
            databaseCheckBox.setSelected(false);
        });
    }

    public boolean isDatabaseSelected() {
        return databaseCheckBox != null && databaseCheckBox.isSelected();
    }

    public boolean isScriptSelected() {
        return scriptCheckBox != null && scriptCheckBox.isSelected();
    }
}
