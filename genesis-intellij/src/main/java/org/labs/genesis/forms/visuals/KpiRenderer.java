package org.labs.genesis.forms.visuals;

import org.labs.genesis.forms.theme.DashboardTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Renderer pour une visualisation KPI.
 *
 * Le titre est géré par DashboardVisualComponent.
 * Ce renderer affiche uniquement la valeur et éventuellement
 * une information complémentaire.
 */
public class KpiRenderer implements VisualizationRenderer {

    @Override
    public JComponent createComponent() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        8,
                        18,
                        8,
                        12
                )
        );

        /*
         * Valeur principale.
         */
        JLabel valueLabel =
                new JLabel("27 351 €");

        valueLabel.setFont(
                DashboardTheme.boldFont(26)
        );

        valueLabel.setForeground(DashboardTheme.TEXT_DARK_SECONDARY);

        /*
         * Permet au texte de rester correctement positionné
         * lorsque le composant est redimensionné.
         */
        valueLabel.setHorizontalAlignment(
                SwingConstants.LEFT
        );
        /*
         * Conteneur vertical.
         */
        JPanel content =
                new JPanel();

        content.setOpaque(false);

        content.setLayout(
                new BoxLayout(
                        content,
                        BoxLayout.Y_AXIS
                )
        );

        content.add(valueLabel);

        panel.add(
                content,
                BorderLayout.CENTER
        );

        return panel;
    }

    @Override
    public String getDisplayName() {
        return "KPI";
    }
}