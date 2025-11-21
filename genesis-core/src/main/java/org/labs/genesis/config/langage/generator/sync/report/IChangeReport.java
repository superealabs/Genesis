package org.labs.genesis.config.langage.generator.sync.report;

public interface IChangeReport {
    public void onAddTable();
    public void onRemoveTable();
    public void onTableModification();
}
