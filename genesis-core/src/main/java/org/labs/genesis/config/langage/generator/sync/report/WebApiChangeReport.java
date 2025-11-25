package org.labs.genesis.config.langage.generator.sync.report;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebApiChangeReport implements IChangeReport {
    public Boolean generateAdditionalFiles = false;
    public Boolean generateModel = false;
    public Boolean generateDAO = false;
    public Boolean generateService = false;
    public Boolean generateController = false;

    @Override
    public void onAddTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = true;
        this.generateDAO = true;
        this.generateService = true;
        this.generateController = true;
    }

    @Override
    public void onRemoveTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = false;
        this.generateDAO = false;
        this.generateService = false;
        this.generateController = false;
    }

    @Override
    public void onUpdateTable() {
        this.generateAdditionalFiles = false;
        this.generateModel = true;
        this.generateDAO = true;
        this.generateService = true;
        this.generateController = true;
    }
}
