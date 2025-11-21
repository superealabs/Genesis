package org.labs.genesis.config.langage.generator.sync.report;

public class FrontendChangeReport implements IChangeReport {
    public Boolean generateAdditionalFiles = false;
    public Boolean generateModel = false;
    public Boolean generateService = false;
    public Boolean generateComponents = false;

    @Override
    public void onAddTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = true;
        this.generateComponents = true;
        this.generateService = true;
    }

    @Override
    public void onRemoveTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = false;
        this.generateComponents = false;
        this.generateService = false;
    }

    @Override
    public void onTableModification() {
        throw new UnsupportedOperationException("On table modification Not supported yet.");
    }
}
