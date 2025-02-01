package org.labs.genesis;

public class Utils {
    public static String formatErrorMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "Unknown error.";
        }
        // Split the message by ". " (period followed by a space) and join with <br> for HTML
        return String.join("\n", message.split("\\.\\s"));
    }

    public static String formatErrorMessageHtml(String message) {
        if (message == null || message.isEmpty()) {
            return "Unknown error.";
        }
        // Split the message by ". " (period followed by a space) and join with <br> for HTML
        return String.join("<br>", message.split("\\.\\s"));
    }
}
