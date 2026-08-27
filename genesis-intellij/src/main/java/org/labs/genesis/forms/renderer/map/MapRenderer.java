package org.labs.genesis.forms.renderer.map;

import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import org.labs.genesis.forms.renderer.AbstractComponentRenderer;

import javax.swing.*;
import java.awt.*;

public class MapRenderer
        extends AbstractComponentRenderer {

    private JBCefBrowser browser;

    @Override
    protected JComponent createSwingComponent() {

        JPanel panel =
                new JPanel(new BorderLayout());

        panel.setOpaque(false);

        // ========================================================
        // PADDING AUTOUR DE LA CARTE
        // ========================================================

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        12, // top
                        12, // left
                        12, // bottom
                        12  // right
                )
        );

        if (!JBCefApp.isSupported()) {

            panel.add(
                    createFallbackPanel(),
                    BorderLayout.CENTER
            );

            return panel;
        }

        browser =
                new JBCefBrowser();

        panel.add(
                browser.getComponent(),
                BorderLayout.CENTER
        );

        browser.loadHTML(
                createLeafletHtml()
        );

        return panel;
    }

    // ============================================================
    // FALLBACK (JCEF indisponible)
    // ============================================================

    /**
     * Message de repli plus soigné qu'un simple JLabel par défaut,
     * avec une hiérarchie (titre + sous-texte) et un espacement
     * cohérent avec le reste du dashboard.
     */
    private JComponent createFallbackPanel() {

        JPanel wrapper =
                new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(
                new BoxLayout(wrapper, BoxLayout.Y_AXIS)
        );
        wrapper.setBorder(
                BorderFactory.createEmptyBorder(24, 16, 24, 16)
        );

        JLabel title =
                new JLabel("Carte indisponible");
        title.setFont(
                title.getFont().deriveFont(Font.BOLD, 13f)
        );
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitle =
                new JLabel("JCEF n'est pas disponible sur cet environnement");
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
    // CARTE
    // ============================================================

    private String createLeafletHtml() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta
                        name="viewport"
                        content="width=device-width,
                                 initial-scale=1.0"
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

                        /* Popup épuré : coins arrondis, ombre douce,
                           typographie cohérente avec un dashboard
                           moderne plutôt que le popup blanc par
                           défaut de Leaflet. */
                        .leaflet-popup-content-wrapper {
                            border-radius: 10px;
                            box-shadow: 0 6px 20px rgba(15, 23, 42, 0.15);
                            padding: 0;
                        }
                        .leaflet-popup-content {
                            margin: 10px 14px;
                            font-family: -apple-system, "Segoe UI", Roboto, sans-serif;
                        }
                        .leaflet-popup-tip {
                            box-shadow: 0 4px 10px rgba(15, 23, 42, 0.1);
                        }
                        .popup-city {
                            font-weight: 600;
                            font-size: 13px;
                            color: #0f172a;
                            margin-bottom: 4px;
                        }
                        .popup-value {
                            display: inline-block;
                            font-weight: 600;
                            font-size: 12px;
                            color: #4f46e5;
                            background: #eef2ff;
                            border-radius: 999px;
                            padding: 2px 9px;
                        }

                        .leaflet-control-zoom {
                            border-radius: 8px !important;
                            overflow: hidden;
                            box-shadow: 0 2px 8px rgba(15, 23, 42, 0.15) !important;
                        }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        const map = L.map('map', {
                            zoomControl: false
                        });

                        L.control.zoom({ position: 'bottomright' }).addTo(map);

                        /* Fond de carte clair et minimal (CartoDB
                           Positron) plutôt que le rendu OSM par
                           défaut, plus chargé visuellement et moins
                           adapté à un dashboard où les marqueurs
                           doivent rester le point focal. */
                        L.tileLayer(
                            'https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png',
                            {
                                maxZoom: 19,
                                attribution:
                                    '&copy; OpenStreetMap contributors &copy; CARTO'
                            }
                        ).addTo(map);

                        const points = [
                            { name: 'Paris',     lat: 48.8566, lng: 2.3522, value: 45 },
                            { name: 'Lyon',       lat: 45.7640, lng: 4.8357, value: 28 },
                            { name: 'Marseille',  lat: 43.2965, lng: 5.3698, value: 32 }
                        ];

                        const maxValue = Math.max(...points.map(p => p.value));

                        /* Rayon proportionnel à la valeur plutôt que
                           des pins uniformes : transforme la carte
                           en mini bubble-map, ce qui montre l'écart
                           entre villes d'un simple coup d'œil. */
                        function radiusFor(value) {
                            const minR = 8;
                            const maxR = 22;
                            return minR + (maxR - minR) * Math.sqrt(value / maxValue);
                        }

                        const bounds = [];

                        points.forEach(p => {
                            const marker = L.circleMarker([p.lat, p.lng], {
                                radius: radiusFor(p.value),
                                weight: 2,
                                color: '#4f46e5',
                                fillColor: '#818cf8',
                                fillOpacity: 0.55
                            }).addTo(map);

                            marker.bindPopup(
                                '<div class="popup-city">' + p.name + '</div>' +
                                '<span class="popup-value">' + p.value + ' k\u20ac</span>'
                            );

                            bounds.push([p.lat, p.lng]);
                        });

                        map.fitBounds(bounds, { padding: [40, 40], maxZoom: 7 });
                    </script>
                </body>
                </html>
                """;
    }

    public JBCefBrowser getBrowser() {
        return browser;
    }
}