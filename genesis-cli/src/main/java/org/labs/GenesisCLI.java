package org.labs;

import org.labs.handler.ProjectGeneratorHandler;

public class GenesisCLI {
    public static void main(String[] args) throws Exception {
        ProjectGeneratorHandler projectGeneratorHandler = new ProjectGeneratorHandler();
        projectGeneratorHandler.generateProject();
    }
}

