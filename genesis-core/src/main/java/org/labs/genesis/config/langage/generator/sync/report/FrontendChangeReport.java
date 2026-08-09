package org.labs.genesis.config.langage.generator.sync.report;

public class FrontendChangeReport {
    public Boolean generateAdditionalFiles = false;
    public Boolean generateModel = false;
    public Boolean generateService = false;
    public Boolean generateComponents = false;

    public void onAddTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = true;
        this.generateComponents = true;
        this.generateService = true;
    }

    public void onRemoveTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = false;
        this.generateComponents = false;
        this.generateService = false;
    }

    public void onUpdateTable(Boolean model, Boolean components, Boolean service) {
        this.generateAdditionalFiles = false;
        this.generateModel = model;
        this.generateComponents = components;
        this.generateService = service;
    }
}
