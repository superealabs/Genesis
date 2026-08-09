package org.labs.genesis.config.langage.generator.sync.report;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebApiChangeReport {
    public Boolean generateAdditionalFiles;
    public Boolean generateModel;
    public Boolean generateDAO;
    public Boolean generateService;
    public Boolean generateController;

    public WebApiChangeReport() {
        this(false, false, false, false, false);
    }

    public WebApiChangeReport(Boolean generateAdditionalFiles, Boolean generateModel, Boolean generateDAO, Boolean generateService, Boolean generateController) {
        this.generateAdditionalFiles = generateAdditionalFiles;
        this.generateModel = generateModel;
        this.generateDAO = generateDAO;
        this.generateService = generateService;
        this.generateController = generateController;
    }

    public void onAddTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = true;
        this.generateDAO = true;
        this.generateService = true;
        this.generateController = true;
    }

    public void onRemoveTable() {
        this.generateAdditionalFiles = true;
        this.generateModel = false;
        this.generateDAO = false;
        this.generateService = false;
        this.generateController = false;
    }

    public void onUpdateTable(Boolean model, Boolean dao, Boolean service, Boolean controller) {
        this.generateAdditionalFiles = false;
        this.generateModel = model;
        this.generateDAO = dao;
        this.generateService = service;
        this.generateController = controller;
    }

}
