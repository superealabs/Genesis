package org.labs.genesis.connexion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class ScriptDatabaseChecker {

    private ScriptDatabaseChecker() {
    }

    /**
     * @return null si le script correspond à la base, sinon un message d'erreur clair.
     */
    public static String mismatchMessage(Database database, String script, Collection<Database> allDatabases) {
        Set<String> unknown = unknownTypes(database, script);
        Set<String> foreign = foreignSyntax(database, script);
        if (unknown.isEmpty() && foreign.isEmpty()) {
            return null;
        }
        List<String> matching = matchingDatabases(script, allDatabases).stream()
                .map(Database::getName)
                .toList();
        String hint = matching.isEmpty()
                ? "Corrigez ces types ou choisissez une autre base."
                : "Ce script correspond plutôt à : " + String.join(", ", matching) + ".";
        List<String> raisons = new ArrayList<>();
        if (!unknown.isEmpty()) {
            raisons.add("types inconnus " + unknown);
        }
        if (!foreign.isEmpty()) {
            raisons.add("syntaxe propre à une autre base " + foreign);
        }
        return "Le script ne correspond pas à la base " + database.getName()
                + " : " + String.join(", ", raisons) + ". " + hint;
    }

    private static final Map<String, Map<String, Pattern>> SYNTAXE_PROPRE = Map.of(
            "mysql", Map.of(
                    "AUTO_INCREMENT", Pattern.compile("\\bAUTO_INCREMENT\\b", Pattern.CASE_INSENSITIVE),
                    "ENGINE=", Pattern.compile("\\)\\s*ENGINE\\s*=", Pattern.CASE_INSENSITIVE),
                    "`nom`", Pattern.compile("`[^`\\n]+`")),
            "sql server", Map.of(
                    "IDENTITY(n,n)", Pattern.compile("\\bIDENTITY\\s*\\(\\s*\\d+\\s*,\\s*\\d+\\s*\\)", Pattern.CASE_INSENSITIVE)));

    public static Set<String> foreignSyntax(Database database, String script) {
        Set<String> foreign = new LinkedHashSet<>();
        if (database == null || database.getName() == null || script == null) {
            return foreign;
        }
        String sql = sansCommentaires(script);
        String choisie = database.getName().toLowerCase(Locale.ROOT);
        SYNTAXE_PROPRE.forEach((base, motifs) -> {
            if (!base.equals(choisie)) {
                motifs.forEach((motCle, motif) -> {
                    if (motif.matcher(sql).find()) {
                        foreign.add(motCle);
                    }
                });
            }
        });
        return foreign;
    }

    private static String sansCommentaires(String sql) {
        return sql.replaceAll("(?s)/\\*.*?\\*/", " ").replaceAll("--[^\\n]*", " ");
    }

    public static List<Database> matchingDatabases(String script, Collection<Database> allDatabases) {
        List<Database> matching = new ArrayList<>();
        for (Database db : allDatabases) {
            if (unknownTypes(db, script).isEmpty() && foreignSyntax(db, script).isEmpty()) {
                matching.add(db);
            }
        }
        return matching;
    }


    public static Set<String> unknownTypes(Database database, String script) {
        Set<String> unknown = new LinkedHashSet<>();
        if (database == null || database.getTypes() == null || script == null) {
            return unknown;
        }
        for (String body : createTableBodies(script)) {
            for (String clause : SQLRunner.splitClauses(body)) {
                String line = clause.trim();
                String upper = line.toUpperCase();
                if (upper.startsWith("PRIMARY KEY") || upper.startsWith("FOREIGN KEY") || upper.startsWith("CONSTRAINT")
                        || upper.startsWith("UNIQUE") || upper.startsWith("CHECK") || upper.startsWith("KEY")
                        || upper.startsWith("INDEX")) {
                    continue;
                }
                String[] tokens = line.split("\\s+");
                if (tokens.length < 2) {
                    continue;
                }
                String type = cleanType(tokens[1]);
                boolean known = database.getTypes().containsKey(database.normalizeColumnType(type));
                StringBuilder candidate = new StringBuilder(type);
                for (int i = 2; !known && i < Math.min(tokens.length, 5); i++) {
                    candidate.append(' ').append(cleanType(tokens[i]));
                    known = database.getTypes().containsKey(database.normalizeColumnType(candidate.toString()));
                }
                if (!known) {
                    unknown.add(type.toUpperCase());
                }
            }
        }
        return unknown;
    }

    private static String cleanType(String token) {
        String t = token.replaceAll("[\"`\\[\\]]", "").trim();
        return t.contains("(") ? t.substring(0, t.indexOf('(')).trim() : t;
    }


    private static List<String> createTableBodies(String sql) {
        List<String> bodies = new ArrayList<>();
        for (String query : SQLRunner.removeComments(sql).split(";")) {
            String trimmed = query.trim();
            int idx = trimmed.toUpperCase().indexOf("CREATE TABLE");
            if (idx == -1) {
                continue;
            }
            String after = trimmed.substring(idx + "CREATE TABLE".length());
            int open = after.indexOf('(');
            if (open == -1) {
                continue;
            }
            int depth = 0;
            for (int i = open; i < after.length(); i++) {
                char c = after.charAt(i);
                if (c == '(') {
                    depth++;
                } else if (c == ')' && --depth == 0) {
                    bodies.add(after.substring(open + 1, i));
                    break;
                }
            }
        }
        return bodies;
    }
}
