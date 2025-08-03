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


    public static  String pluralize(String string) {
        return string+"s";
    }

    public  static String majPluralize(String string) {
        return  pluralize(majStart(string));
    }

    public  static String minPluralize(String string) {
        return pluralize(minStart(string));
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

    public static String toPascalCase(String string) {
        return string.transform(s -> {
            // Remplace tirets et underscores par un séparateur commun
            String[] words = s.split("[-_]+");
            StringBuilder pascalCase = new StringBuilder();

            for (String word : words) {
                pascalCase.append(majStart(word));
            }

            return pascalCase.toString();
        });
    }


    public static String toKebabCase(String input) {
        if (input == null || input.isEmpty()) return input;

        input = input.replace("_", "-");

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (Character.isUpperCase(c)) {
                if (i > 0 && input.charAt(i - 1) != '-' && !Character.isUpperCase(input.charAt(i - 1))) {
                    result.append("-");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        return result.toString();
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

    public static String skipEscape(String s ){
        if (s == null) {
            return s;
        }
        return  s.replaceAll(" ","");
    }

    public static String replaceUntilMarker(String input, String marker, String replacement) {
        int index = input.indexOf(marker);
        if (index != -1) {
            return replacement + input.substring(index + marker.length());
        }
        return input;
    }
}
