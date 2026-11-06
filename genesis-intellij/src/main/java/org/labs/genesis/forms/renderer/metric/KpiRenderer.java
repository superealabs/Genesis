package org.labs.genesis.forms.renderer.metric;

import org.labs.genesis.forms.renderer.VisualizationRenderer;
import org.labs.genesis.forms.renderer.provider.ChartData;
import org.labs.genesis.forms.theme.DashboardTheme;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

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

        String prefix = config.getString("valuePrefix", "");
        String suffix = config.getString("valueSuffix", "");
        int decimals = getDecimals(config);

        valueLabel.setText(formatValue(value, prefix, suffix, decimals));
    }

    private String formatValue(double value, String prefix, String suffix, int decimals) {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(decimals);
        formatter.setMaximumFractionDigits(decimals);
        String formatted = formatter.format(value);
        return prefix + formatted + suffix;
    }

    private int getDecimals(VisualizationConfig config) {

        Object value = config.getValue("decimals");

        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }

        if (value != null) {
            try {
                return Math.max(0, Integer.parseInt(value.toString()));
            } catch (NumberFormatException ignored) {
                // valeur par défaut
            }
        }

        return 0;
    }

    @Override
    public String getDisplayName() {
        return "KPI";
    }
}