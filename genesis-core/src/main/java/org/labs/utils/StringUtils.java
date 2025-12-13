package org.labs.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtils {

    public static String removeLastS(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        char lastChar = input.charAt(input.length() - 1);
        if (lastChar == 's' || lastChar == 'S') {
            return input.substring(0, input.length() - 1);
        }

        return input;
    }


    public static String minStart(String string) {
        return string.transform(s -> s.replaceFirst(String.valueOf(s.charAt(0)), String.valueOf(s.charAt(0)).toLowerCase()));
    }


    public static String majStart(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Renvoie null ou chaîne vide pour éviter NullPointerException
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    public static String toCamelCase(String string) {
        return string.transform(s -> {
            String[] words = s.split("_");
            StringBuilder camelCase = new StringBuilder(words[0].toLowerCase());

            for (int i = 1; i < words.length; i++) {
                camelCase.append(majStart(words[i]));
            }

            return camelCase.toString();
        });
    }

    public static String toKebabCase(String input) {
        return input.transform(s -> {
            if (s.isEmpty()) {
                return s;
            }

            StringBuilder result = new StringBuilder();
            s = s.replace("_", "");

            for (char c : s.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    if (!result.isEmpty()) {
                        result.append("-");
                    }
                    result.append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            }

            return result.toString();
        });
    }


    public static String baseFormat(String s) {
        String newString = s.replace("_", " ");
        StringBuilder newWord = new StringBuilder();

        for (int i = 0; i < newString.length(); i++) {
            char c = newString.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0 && newWord.charAt(newWord.length() - 1) != ' ') {
                    newWord.append(" ");
                }
                newWord.append(Character.toLowerCase(c));
            } else {
                newWord.append(c);
            }
        }

        return newWord.toString().replaceAll("\\s+", " ").trim();
    }

    public static String formatReadable(String s) {
        return majStart(baseFormat(s));
    }

    public static String formatReadableLowerCase(String s) {
        return baseFormat(s).toLowerCase();
    }

    public static String quoteAndJoin(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        return Arrays.stream(array)
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.joining(","));
    }

    public static String join(String[] array) {
        if (array == null || array.length == 0) {
            return "";
        }
        return String.join(",", array);
    }


    public static String escapeAccents(String input) {
        if (input == null) return null;

        return input
            .replace("é", "&eacute;")
            .replace("è", "&egrave;")
            .replace("ê", "&ecirc;")
            .replace("à", "&agrave;")
            .replace("â", "&acirc;")
            .replace("ù", "&ugrave;")
            .replace("û", "&ucirc;")
            .replace("î", "&icirc;")
            .replace("ô", "&ocirc;")
            .replace("ç", "&ccedil;")
            .replace("É", "&Eacute;")
            .replace("È", "&Egrave;")
            .replace("Ê", "&Ecirc;")
            .replace("À", "&Agrave;")
            .replace("Â", "&Acirc;")
            .replace("Ç", "&Ccedil;");
    }

    public static String relativeOrFilename(String root, String filePath) {
        if (root == null || filePath == null) {
            return null;
        }
        Path rootPath = Paths.get(root).normalize();
        Path targetPath = Paths.get(filePath).normalize();
        if (targetPath.startsWith(rootPath)) {
            return rootPath.relativize(targetPath).toString();
        }
        return targetPath.getFileName().toString();
    }
}
