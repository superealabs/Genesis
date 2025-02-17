package org.labs.utils;

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

}
