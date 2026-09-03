package org.labs.genesis.forms.renderer.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record ChartData(
        List<String> labels,
        double[] values,
        double[][] points
) {

    public static final String CONFIG_KEY = "__ChartData";
    public static final String ERROR_KEY = "__chartError";
    public static final String LOADING_KEY = "__chartLoading";

    public ChartData {
        labels = labels != null ? Collections.unmodifiableList(new ArrayList<>(labels)) : Collections.emptyList();
        values = values != null ? values.clone() : new double[0];
        points = copyPoints(points);
    }

    public static ChartData series(List<String> labels, double[] values) {
        return new ChartData(labels, values, null);
    }

    public static ChartData values(double[] values) {
        if (values == null) {
            return new ChartData(Collections.emptyList(), new double[0], null);
        }

        List<String> labels = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            labels.add(String.valueOf(i));
        }
        return series(labels, values);
    }

    public static ChartData points(double[][] points) {
        return new ChartData(Collections.emptyList(), new double[0], points);
    }

    public static ChartData points(List<String> labels, double[][] points) {
        return new ChartData(labels, new double[0], points);
    }

    public static ChartData empty() {
        return new ChartData(Collections.emptyList(), new double[0], null);
    }

    public boolean hasSeries() {
        return !labels.isEmpty() || values.length > 0;
    }

    public boolean hasPoints() {
        return points.length > 0;
    }

    public boolean isEmpty() {
        return labels.isEmpty() && values.length == 0 && points.length == 0;
    }

    public int size() {
        if (hasPoints()) return points.length;
        return Math.max(labels.size(), values.length);
    }

    public String labelAt(int index) {
        return (index >= 0 && index < labels.size()) ? labels.get(index) : null;
    }

    public double valueAt(int index) {
        return (index >= 0 && index < values.length) ? values[index] : Double.NaN;
    }

    public double[] pointAt(int index) {
        return (index >= 0 && index < points.length) ? points[index].clone() : null;
    }

    public boolean hasValidSeries() {
        return labels.size() == values.length;
    }

    public boolean hasValidPoints() {
        if (points.length == 0) return true;
        for (double[] point : points) {
            if (point == null || point.length != 2) return false;
        }
        return true;
    }

    public boolean isValid() {
        return hasValidSeries() && hasValidPoints();
    }

    private static double[][] copyPoints(double[][] source) {
        if (source == null || source.length == 0) return new double[0][];

        double[][] copy = new double[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i] != null ? source[i].clone() : null;
        }
        return copy;
    }
}