package org.labs.genesis.forms.ui.visualization.model;

import org.labs.genesis.forms.renderer.VisualizationRenderer;

import java.util.List;

/**
 * Représente un élément de visualisation disponible dans le panneau.
 * Contient toutes les métadonnées nécessaires pour afficher et configurer
 * une visualisation dans l'interface.
 */
public class VisualizationItem {

    /**
     * Le nom affiché de la visualisation (ex: "Bar Chart Vertical")
     */
    public final String name;

    /**
     * La description de la visualisation (affichée dans l'infobulle)
     */
    public final String description;

    /**
     * Le chemin vers l'image d'aperçu de la visualisation
     */
    public final String imagePath;

    /**
     * La classe du renderer qui va générer la visualisation
     */
    public final Class<? extends VisualizationRenderer> rendererClass;

    /**
     * La liste des paramètres configurables pour cette visualisation
     */
    public final List<VisualizationParameter> parameters;

    /**
     * Constructeur complet pour créer un élément de visualisation.
     *
     * @param name           Le nom affiché de la visualisation
     * @param description    La description de la visualisation
     * @param imagePath      Le chemin vers l'image d'aperçu
     * @param rendererClass  La classe du renderer
     * @param parameters     La liste des paramètres configurables
     */
    public VisualizationItem(String name, String description, String imagePath,
                             Class<? extends VisualizationRenderer> rendererClass,
                             List<VisualizationParameter> parameters) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.rendererClass = rendererClass;
        this.parameters = parameters;
    }

    /**
     * Vérifie si cette visualisation a un paramètre spécifique.
     *
     * @param key La clé du paramètre à vérifier
     * @return true si le paramètre existe, false sinon
     */
    public boolean hasParameter(String key) {
        if (parameters == null) {
            return false;
        }
        return parameters.stream().anyMatch(p -> p.getKey().equals(key));
    }

    /**
     * Récupère un paramètre par sa clé.
     *
     * @param key La clé du paramètre à récupérer
     * @return Le paramètre trouvé, ou null s'il n'existe pas
     */
    public VisualizationParameter getParameter(String key) {
        if (parameters == null) {
            return null;
        }
        return parameters.stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    /**
     * Récupère la valeur par défaut d'un paramètre.
     *
     * @param key La clé du paramètre
     * @return La valeur par défaut, ou null si le paramètre n'existe pas
     */
    public Object getDefaultValue(String key) {
        VisualizationParameter param = getParameter(key);
        return param != null ? param.getValue() : null;
    }

    /**
     * Crée une nouvelle instance du renderer pour cette visualisation.
     *
     * @return Une nouvelle instance du renderer, ou null en cas d'erreur
     */
    public VisualizationRenderer createRenderer() {
        try {
            return rendererClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Vérifie si cette visualisation est compatible avec un type de données.
     *
     * @param dataType Le type de données à vérifier
     * @return true si compatible, false sinon
     */
    public boolean isCompatibleWith(String dataType) {
        // Logique de compatibilité à implémenter selon les besoins
        // Par défaut, on considère que tout est compatible
        return true;
    }

    /**
     * Retourne le nombre de paramètres requis.
     *
     * @return Le nombre de paramètres requis
     */
    public int getRequiredParameterCount() {
        if (parameters == null) {
            return 0;
        }
        return (int) parameters.stream()
                .filter(VisualizationParameter::isRequired)
                .count();
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean hasAllRequiredParameters(VisualizationConfig config) {
        if (parameters == null || parameters.isEmpty()) {
            return true;
        }

        return parameters.stream()
                .filter(VisualizationParameter::isRequired)
                .allMatch(param -> {
                    Object value = config.getValue(param.getKey());
                    if (value == null) return false;
                    if (value instanceof String && ((String) value).trim().isEmpty()) return false;
                    if (value instanceof List && ((List<?>) value).isEmpty()) return false;
                    System.out.println("--> " + value + "-> " + param.getKey());
                    return true;
                });
    }

    /**
     * Builder pour faciliter la création d'instances de VisualizationItem.
     */
    public static class Builder {
        private String name;
        private String description;
        private String imagePath;
        private Class<? extends VisualizationRenderer> rendererClass;
        private List<VisualizationParameter> parameters;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder imagePath(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public Builder rendererClass(Class<? extends VisualizationRenderer> rendererClass) {
            this.rendererClass = rendererClass;
            return this;
        }

        public Builder parameters(List<VisualizationParameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public VisualizationItem build() {
            return new VisualizationItem(name, description, imagePath, rendererClass, parameters);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualizationItem that = (VisualizationItem) o;

        if (!name.equals(that.name)) return false;
        if (!description.equals(that.description)) return false;
        if (!imagePath.equals(that.imagePath)) return false;
        if (!rendererClass.equals(that.rendererClass)) return false;
        return parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + imagePath.hashCode();
        result = 31 * result + rendererClass.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }
}