package org.labs.genesis.forms;

import lombok.Getter;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.RelationParameter;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.renderer.TableMetadataListCellRenderer;
import org.labs.genesis.renderer.TableMetadataTableCellRenderer;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

@Getter
public class RelationshipConfigurationForm {
    private JPanel mainPanel;
    private JLabel titleLabel;
    private JPanel titlePanel;
    private JPanel configPanel;
    private JComboBox<TableMetadata> childSelect;
    private JComboBox<TableMetadata> parentSelect;
    private JButton addRelationButton;
    private JTable relationTable;
    private JCheckBox skipCheckBox;
    private JScrollPane tableScrollPane;
    private JButton removeLineButton;

    private final List<RelationParameter> relationParameters;

    public RelationshipConfigurationForm(){
        relationParameters = new ArrayList<>();
        initForm();
    }

    private void initForm(){
        assert childSelect != null;
        childSelect.setRenderer(new TableMetadataListCellRenderer());
        assert parentSelect != null;
        parentSelect.setRenderer(new TableMetadataListCellRenderer());
        initializeListners();
        initializeTable();
    }



    public void populateSelect(Dictionary<String,List<TableMetadata>> relations) {
        List<TableMetadata> parents = relations.get("PARENTS");
        List<TableMetadata> children = relations.get("CHILDS");
        if (parents.isEmpty() || children.isEmpty()) {
            skipCheckBox.setSelected(true);
        }
        childSelect.setModel(new DefaultComboBoxModel(children.toArray()));
        parentSelect.setModel(new DefaultComboBoxModel(parents.toArray()));
    }

    private void initializeListners(){
        addRelationButton.addActionListener(e -> addRelation());
        removeLineButton.addActionListener(e -> removeRelation());
    }

    private void initializeTable() {
        String[] columnNames = {"Parent Entity", "Child Entity", "Is Mandatory"};
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        relationTable.setModel(model);

        TableCellRenderer tableMetadataRenderer = new TableMetadataTableCellRenderer();
        relationTable.getColumnModel().getColumn(0).setCellRenderer(tableMetadataRenderer);
        relationTable.getColumnModel().getColumn(1).setCellRenderer(tableMetadataRenderer);

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                    int row = e.getFirstRow();
                    Object newValue = model.getValueAt(row, 2);
                    System.out.println("Valeur modifiée à la ligne " + row + ": " + newValue);
                    handleMandatoryChange(row, newValue);
                }
            }
        });
    }

    private void handleMandatoryChange(int row, Object newValue) {
        if (row >= 0 && row < relationParameters.size() && newValue instanceof Boolean isMandatory) {
            RelationParameter relation = relationParameters.get(row);
            relation.setMandatory(isMandatory);
            System.out.println("Relation mise à jour: " + relation.getParentTable() +
                    " -> " + relation.getChildTable() +
                    " (Mandatory: " + isMandatory + ")");
        }
    }

    private void addRelation(){
        TableMetadata childSelected = (TableMetadata) childSelect.getSelectedItem();
        TableMetadata parentSelected = (TableMetadata) parentSelect.getSelectedItem();
        if (childSelected == null || parentSelected == null){
            return;
        }
        ColumnMetadata fkColumn = childSelected.findForeingKeyColumnByClassName(parentSelected.getClassName());
        if (childSelected.equals(parentSelected)){
            JOptionPane.showMessageDialog(mainPanel, "Cannot set a relation from a table to itself", "", JOptionPane.WARNING_MESSAGE);
            return;
        }
        else if (fkColumn == null){
            JOptionPane.showMessageDialog(mainPanel, "The table "+parentSelected.getTableName()+" has no relation to "+childSelected.getTableName(), "Relation invalid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        RelationParameter currentRelation = new RelationParameter(
                parentSelected.getTableName(),
                childSelected.getTableName(),
                !fkColumn.isNullable()
        );
        if (relationParameters.contains(currentRelation)){
            JOptionPane.showMessageDialog(mainPanel, "This relation already exists.", "Duplicate Relation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        this.relationParameters.add(currentRelation);
        DefaultTableModel model = (DefaultTableModel) relationTable.getModel();
        model.addRow(currentRelation.toRow());
    }

    private void removeRelation() {
        int selectedRow = relationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainPanel,
                    "Veuillez sélectionner la ligne à retirer.",
                    "Aucune ligne sélectionnée",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        DefaultTableModel model = (DefaultTableModel) relationTable.getModel();
        if (selectedRow < relationParameters.size()) {
            relationParameters.remove(selectedRow);
        }
        model.removeRow(selectedRow);
    }
}