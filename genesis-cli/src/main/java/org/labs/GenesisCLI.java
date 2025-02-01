package org.labs;

import org.labs.handler.ProjectGeneratorHandler;

public class GenesisCLI {
    public static void main(String[] args) {
        ProjectGeneratorHandler projectGeneratorHandler = new ProjectGeneratorHandler();
        projectGeneratorHandler.generateProject();
    }
}

