package org.labs.genesis.frontend.generator.resources;

import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.model.TableMetadata;

import java.util.HashMap;
import java.util.List;

public interface IResourceGenerator {
    public  String generateRessources(ProjectGenerationContext context, HashMap<String, Object> metadata)throws  Exception;
}
