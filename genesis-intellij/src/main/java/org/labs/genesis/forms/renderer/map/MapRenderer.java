package org.labs.genesis.forms.renderer.map;

import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import org.labs.genesis.forms.renderer.AbstractComponentRenderer;
import org.labs.genesis.forms.renderer.provider.MapData;
import org.labs.genesis.forms.ui.visualization.model.VisualizationConfig;

import javax.swing.*;
import java.awt.*;

public class MapRenderer extends AbstractComponentRenderer {

    private JBCefBrowser browser;
    // ============================================================
    // COMPOSANT
    // ============================================================

    @Override
    protected JComponent createSwingComponent() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setOpaque(false);
        panel.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        if (!JBCefApp.isSupported()) {
            panel.add(
                    createFallbackPanel(),
                    BorderLayout.CENTER
            );

            return panel;
        }

        browser = new JBCefBrowser();
        configureMapCursor();

        panel.add(
                browser.getComponent(),
                BorderLayout.CENTER
        );

        updateMap();

        return panel;
    }

    @Override
    public void updateConfig(VisualizationConfig config) {
        this.config = config;
        updateMap();
    }

    private void updateMap() {

        MapData loading = getMapLoading();

        if (loading != null) {
            browser.loadHTML(
                    createMessageHtml("Chargement...")
            );
            return;
        }

        MapData error = getMapError();
        if (error != null) {
            browser.loadHTML(
                    createMessageHtml(
                            error.toString()
                    )
            );
            return;
        }

        browser.loadHTML(createLeafletHtml());
    }

    private String escapeHtml(String value) {

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String createMessageHtml(String message) {

        String safeMessage = escapeHtml(
                message == null || message.isBlank()
                        ? "Données insuffisantes"
                        : message
        );

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">

                <style>
                    html,
                    body {
                        margin: 0;
                        padding: 0;
                        width: 100%%;
                        height: 100%%;
                        overflow: hidden;
                        background: #f8fafc;
                    }

                    body {
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-family: Arial, sans-serif;
                    }

                    .message {
                        color: #64748b;
                        font-size: 14px;
                        text-align: center;
                        padding: 20px;
                    }
                </style>
            </head>

            <body>
                <div class="message">%s</div>
            </body>
            </html>
            """.formatted(safeMessage);
    }

    // ============================================================
    // DONNÉES
    // ============================================================

    protected MapData getMapData() {
        if (config == null) {
            return null;
        }
        
        Object value = config.getValue(MapData.CONFIG_KEY);

        return value instanceof MapData mapData ? mapData : null;
    }

    protected MapData getMapError() {
        if (config == null) {
            return null;
        }

        Object value = config.getValue(MapData.ERROR_KEY);
        return value instanceof MapData mapData ? mapData : null;
    }

    protected MapData getMapLoading() {
        if (config == null) {
            return null;
        }

        Object value = config.getValue(MapData.LOADING_KEY);
        return value instanceof MapData mapData ? mapData : null;
    }

    // ============================================================
    // FALLBACK
    // ============================================================

    private JComponent createFallbackPanel() {

        JPanel wrapper = new JPanel();

        wrapper.setOpaque(false);
        wrapper.setLayout(
                new BoxLayout(wrapper, BoxLayout.Y_AXIS)
        );

        wrapper.setBorder(
                BorderFactory.createEmptyBorder(
                        24, 16, 24, 16
                )
        );

        JLabel title = new JLabel("Carte indisponible");

        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 13f)
        );

        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle = new JLabel(
                "JCEF n'est pas disponible sur cet environnement"
        );

        subtitle.setFont(
                subtitle.getFont().deriveFont(Font.PLAIN, 11f)
        );

        subtitle.setForeground(Color.GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        wrapper.add(Box.createVerticalGlue());
        wrapper.add(title);
        wrapper.add(Box.createVerticalStrut(4));
        wrapper.add(subtitle);
        wrapper.add(Box.createVerticalGlue());

        return wrapper;
    }

    // ============================================================
    // HTML LEAFLET
    // ============================================================

    private String createLeafletHtml() {
        MapData data = getMapData();
        String pointsJson = toJson(data);

        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">

                <meta
                    name="viewport"
                    content="width=device-width, initial-scale=1.0"
                >

                <link
                    rel="stylesheet"
                    href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
                >

                <script
                    src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js">
                </script>

                <style>
                    html,
                    body {
                        margin: 0;
                        padding: 0;
                        width: 100%;
                        height: 100%;
                        overflow: hidden;
                        background: #f8fafc;
                    }

                    #map {
                        width: 100%;
                        height: 100%;
                        background: #f1f5f9;
                    }

                    .leaflet-control-zoom {
                        border-radius: 8px !important;
                        overflow: hidden;
                        box-shadow:
                            0 2px 8px
                            rgba(15, 23, 42, 0.15) !important;
                    }
                
                    /* Curseur par défaut sur la carte */
                    .leaflet-container {
                        cursor: grab;
                    }
                    
                    .leaflet-container.leaflet-dragging {
                        cursor: grabbing;
                    }
        
                    /* Pendant le déplacement de la carte */
                    .leaflet-container:active {
                        cursor: grabbing;
                    }
                </style>
            </head>

            <body>

                <div id="map"></div>

                <script>

                    const points = __POINTS__;

                    const map = L.map(
                        'map',
                        {
                            zoomControl: false
                        }
                    );

                    L.control.zoom({
                        position: 'bottomright'
                    }).addTo(map);

                    L.tileLayer(
                            'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
                            {
                                maxZoom: 19,
                                attribution: 'Tiles © Esri'
                            }
                        ).addTo(map);

                    // ------------------------------------------------
                    // Valeur maximale utilisée pour dimensionner
                    // les bubbles.
                    // ------------------------------------------------

                    const maxValue = Math.max(
                        1,
                        ...points.map(
                            p => p.value == null ? 1 : p.value
                        )
                    );

                    // ------------------------------------------------
                    // Taille du bubble
                    // ------------------------------------------------

                    function radiusFor(value) {

                        const minRadius = 8;
                        const maxRadius = 30;

                        const v =
                            value == null || value < 0
                                ? 1
                                : value;

                        return minRadius
                            + (maxRadius - minRadius)
                            * Math.sqrt(v / maxValue);
                    }

                    // ------------------------------------------------
                    // Création des points
                    // ------------------------------------------------

                    const bounds = [];

                    points.forEach(p => {

                        if (
                            p.lat == null ||
                            p.lng == null
                        ) {
                            return;
                        }

                        const markerType =
                            p.markerType == null
                                ? "PIN"
                                : p.markerType.toUpperCase();

                        // ============================================
                        // PIN
                        // ============================================

                        if (markerType === "PIN") {

                            L.marker([
                                p.lat,
                                p.lng
                            ]).addTo(map);

                        }

                        // ============================================
                        // BUBBLE
                        // ============================================

                        else {

                            const value =
                                p.value == null
                                    ? 1
                                    : p.value;

                            L.circleMarker(
                                [
                                    p.lat,
                                    p.lng
                                ],
                                {
                                    radius: radiusFor(value),

                                    weight: 2,

                                    color: '#4f46e5',

                                    fillColor: '#818cf8',

                                    fillOpacity: 0.55
                                }
                            ).addTo(map);
                        }

                        bounds.push([
                            p.lat,
                            p.lng
                        ]);
                    });

                    // ------------------------------------------------
                    // Position initiale
                    // ------------------------------------------------

                    if (bounds.length === 1) {

                        map.setView(
                            bounds[0],
                            8
                        );

                    } else if (bounds.length > 1) {

                        map.fitBounds(
                            bounds,
                            {
                                padding: [40, 40],
                                maxZoom: 7
                            }
                        );

                    } else {

                        map.setView(
                            [46.6, 2.5],
                            5
                        );
                    }

                </script>

            </body>
            </html>
            """;

        return template.replace(
                "__POINTS__",
                pointsJson
        );
    }

    private void configureMapCursor() {

        if (browser == null) {
            return;
        }

        Component browserComponent =
                browser.getComponent();

        browserComponent.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );
    }

    // ============================================================
    // JSON
    // ============================================================

    private String toJson(MapData data) {

        if (
                data == null ||
                        data.points() == null ||
                        data.points().isEmpty()
        ) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (MapData.MapPoint point : data.points()) {

            if (point == null) {
                continue;
            }

            if (!first) {
                sb.append(',');
            }

            first = false;

            sb.append('{');

            // ----------------------------------------------------
            // label
            // ----------------------------------------------------

            sb.append("\"label\":");

            if (point.label() == null) {
                sb.append("null");
            } else {
                sb.append('"')
                        .append(escapeJson(point.label()))
                        .append('"');
            }

            sb.append(',');

            // ----------------------------------------------------
            // markerType
            // ----------------------------------------------------

            sb.append("\"markerType\":");

            if (point.markerType() == null) {
                sb.append("null");
            } else {
                sb.append('"')
                        .append(escapeJson(point.markerType()))
                        .append('"');
            }

            sb.append(',');

            // ----------------------------------------------------
            // latitude
            // ----------------------------------------------------

            sb.append("\"lat\":")
                    .append(point.latitude())
                    .append(',');

            // ----------------------------------------------------
            // longitude
            // ----------------------------------------------------

            sb.append("\"lng\":")
                    .append(point.longitude())
                    .append(',');

            // ----------------------------------------------------
            // value
            // ----------------------------------------------------

            sb.append("\"value\":");

            if (point.value() == null) {
                sb.append("null");
            } else {
                sb.append(point.value());
            }

            sb.append('}');
        }

        sb.append(']');

        return sb.toString();
    }

    // ============================================================
    // JSON ESCAPE
    // ============================================================

    private String escapeJson(String value) {

        StringBuilder sb =
                new StringBuilder(value.length() + 8);

        for (int i = 0; i < value.length(); i++) {

            char c = value.charAt(i);

            switch (c) {

                case '"' -> sb.append("\\\"");

                case '\\' -> sb.append("\\\\");

                case '\n' -> sb.append("\\n");

                case '\r' -> sb.append("\\r");

                case '\t' -> sb.append("\\t");

                default -> {

                    if (c < 0x20) {

                        sb.append(
                                String.format(
                                        "\\u%04x",
                                        (int) c
                                )
                        );

                    } else {

                        sb.append(c);
                    }
                }
            }
        }

        return sb.toString();
    }
}