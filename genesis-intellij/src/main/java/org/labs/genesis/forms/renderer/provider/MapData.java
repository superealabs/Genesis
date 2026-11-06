package org.labs.genesis.forms.renderer.provider;

import java.util.List;

public record MapData(List<MapPoint> points, MapMode mode) {
    public enum MapMode { PIN, BUBBLE }
    public static String CONFIG_KEY = "__MAP";
    public static String ERROR_KEY = "__MAP_ERROR";
    public static String LOADING_KEY = "__MAP_LOADING";

    public record MapPoint(
            String label,
            String markerType,
            double latitude,
            double longitude,
            Double value      // nullable : pas nécessaire en mode PIN
    ) {}
}