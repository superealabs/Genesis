package org.example.siteback.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class ExportUtils {

    // Generate CSV from a list of objects using reflection
    public static <T> String generateCsv(List<T> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        Class<?> clazz = data.get(0).getClass();
        Field[] fields = clazz.getDeclaredFields();

        // header
        String header = String.join(",",
                List.of(fields).stream()
                        .map(Field::getName)
                        .collect(Collectors.toList())
        );
        writer.println(header);

        for (T item : data) {
            String line = List.of(fields).stream()
                    .map(field -> {
                        field.setAccessible(true);
                        try {
                            Object value = field.get(item);
                            return value != null ? escapeCsv(value.toString()) : "";
                        } catch (IllegalAccessException e) {
                            return "";
                        }
                    })
                    .collect(Collectors.joining(","));
            writer.println(line);
        }

        writer.flush();
        return stringWriter.toString();
    }
    private static String escapeCsv(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
