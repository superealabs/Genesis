package org.labs.genesis.forms.renderer.metric;

import org.labs.genesis.forms.renderer.VisualizationRenderer;
import org.labs.genesis.forms.renderer.provider.ChartData;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

import javax.swing.*;
import java.awt.*;

public class KpiRenderer implements VisualizationRenderer {

    private JLabel valueLabel;

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

        valueLabel = new JLabel("—");

        valueLabel.setFont(
                DashboardTheme.boldFont(26)
        );

        valueLabel.setForeground(
                DashboardTheme.TEXT_DARK_SECONDARY
        );

        valueLabel.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        JPanel content = new JPanel();

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
    public void updateConfig(VisualizationConfig config) {

        if (valueLabel == null) {
            return;
        }

        Object dataObject =
                config.getValue(ChartData.CONFIG_KEY);

        if (!(dataObject instanceof ChartData)) {
            valueLabel.setText("—");
            return;
        }

        ChartData data = (ChartData) dataObject;

        double[] values = data.values();

        if (values == null || values.length == 0) {
            valueLabel.setText("—");
            return;
        }

        double value = values[0];

        valueLabel.setText(
                formatValue(value)
        );
    }

    private String formatValue(double value) {

        if (value == Math.rint(value)) {
            return String.format(
                    "%,.0f",
                    value
            );
        }

        return String.format(
                "%,.2f",
                value
        );
    }

    @Override
    public String getDisplayName() {
        return "KPI";
    }
}