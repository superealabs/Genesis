package org.example.siteback.services.interfaces;

import java.util.List;

public interface ColumnVisibilityService {

    /**
    * Récupère les champs visibles pour une entité et un type de vue
    * 
    * @param entityName    Nom de l'entité (ex: "personne")
    * @param componentType Type de vue (ex: "list", "detail")
    * @return Liste des champs visibles
    */
    List<String> getVisibleFields(String entityName, String componentType);

    /**
    * Met à jour les champs visibles pour une entité et un type de vue
    * 
    * @param entityName    Nom de l'entité (ex: "personne")
    * @param componentType Type de vue (ex: "list", "detail")
    * @param fields        Liste des champs à afficher
    */
    void updateVisibleFields(String entityName, String componentType, List<String> fields);
}
