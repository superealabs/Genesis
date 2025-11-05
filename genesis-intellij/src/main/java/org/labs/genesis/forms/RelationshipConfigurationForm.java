package org.labs.genesis.forms;

import lombok.Getter;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.RelationParameter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

@Getter
public class RelationshipConfigurationForm {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JPanel configPanel;
    private JComboBox<String> childSelect;
    private JComboBox<String> parentSelect;
    private JButton addRelationButton;
    private JTable relationTable;
    private JCheckBox skipCheckBox;

    private List<RelationParameter> relationParameters;

    public RelationshipConfigurationForm(){
        relationParameters = new ArrayList<>();
        initForm();
    }

    private void initForm(){
        initializeListners();
        initializeTable();
    }

    private void initializeTable(){
        String[] columnNames = {"Child Entity", "Parent Entity", "Is Mandatory"};
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Autoriser l'édition de toutes les cellules
            }
        };
        relationTable.setModel(model);
    }

    private void populateSelect(ProjectGenerationContext context) {
        String[] data = context.getEntityNames().toArray(new String[0]);
        childSelect.setModel(new DefaultComboBoxModel(data));
        parentSelect.setModel(new DefaultComboBoxModel(data));
    }

    private void initializeListners(){
        addRelationButton.addActionListener(e -> addRelation());
    }

    private void addRelation(){
        String childSelected = (String) childSelect.getSelectedItem();
        String parentSelected = (String) parentSelect.getSelectedItem();
        Boolean isMandatory = true;
        if (childSelected != null && !childSelected.isEmpty()){
            return;
        }
        else if (parentSelected != null && !parentSelected.isEmpty()){
            return;
        }
        this.relationParameters.add(new RelationParameter(childSelected, parentSelected, isMandatory));
    }

}
