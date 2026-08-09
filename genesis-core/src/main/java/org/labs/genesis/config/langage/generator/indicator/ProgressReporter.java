package org.labs.genesis.config.langage.generator.indicator;

public interface ProgressReporter {

    /**
     * Définir le texte de progression
     */
    void setText(String text);

    /**
     * Définir le texte secondaire (détails)
     */
    void setText2(String text);

    /**
     * Définir la fraction de progression (0.0 à 1.0)
     */
    void setFraction(double fraction);

    /**
     * Marquer comme indéterminé
     */
    void setIndeterminate(boolean indeterminate);

    /**
     * Vérifier si l'opération a été annulée
     */
    boolean isCanceled();

    /**
     * Vérifier si le reporter est actif
     */
    default boolean isActive() {
        return true;
    }

    default void setProgress(double progress, String message, String details) {
        setText(message);
        setFraction(progress);
        setText2(details);
    }
    default void setProgress(double progress, String message) {
        setProgress(progress, message, "");
    }
}
