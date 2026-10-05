package org.labs.genesis.forms.renderer.provider;

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Construit une Condition jOOQ typée à partir d'une valeur brute (String).
 *
 * Le type réel de la colonne est déterminé via le métadata JDBC et mis en cache.
 * Si le type est introuvable, on retombe sur STRING.
 */
public final class FilterConditionBuilder {

    private enum Kind { STRING, INTEGER, DECIMAL, BOOLEAN, DATE, TIME, DATETIME, UUID }

    private enum Op {
        IS, IS_NOT, CONTAINS, NOT_CONTAINS, STARTS_WITH, ENDS_WITH,
        IS_EMPTY, IS_NOT_EMPTY, GT, GTE, LT, LTE, BETWEEN, IN, NOT_IN;

        static Op parse(String raw) {
            if (raw == null) return IS;
            String v = raw.trim().toLowerCase(Locale.ROOT)
                    .replace('_', ' ')
                    .replaceAll("\\s+", " ");
            return switch (v) {
                case "is", "=", "equals", "eq"                     -> IS;
                case "is not", "!=", "<>", "not equals", "ne"      -> IS_NOT;
                case "contains", "like"                            -> CONTAINS;
                case "doesn't contain", "does not contain",
                     "not contains", "not like"                    -> NOT_CONTAINS;
                case "starts with", "startswith"                   -> STARTS_WITH;
                case "ends with", "endswith"                       -> ENDS_WITH;
                case "is empty"                                    -> IS_EMPTY;
                case "is not empty"                                -> IS_NOT_EMPTY;
                case ">", "greater than", "gt", "after"                            -> GT;
                case ">=", "greater than or equal", "gte", "at least"                       -> GTE;
                case "<", "less than", "lt", "before"                           -> LT;
                case "<=", "less than or equal", "lte", "at most"                        -> LTE;
                case "between"                                     -> BETWEEN;
                case "in", "is one of", "one of"                   -> IN;
                case "not in", "is not one of"                     -> NOT_IN;
                default -> IS;
            };
        }
    }

    private final Connection connection;
    private final Map<String, Kind> cache = new HashMap<>();

    public FilterConditionBuilder(Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    /** Retourne {@code null} si le filtre doit être ignoré. */
    public Condition build(Field<?> field,
                           String table,
                           String column,
                           String operatorRaw,
                           String rawValue) {

        if (field == null) return null;

        Op op = Op.parse(operatorRaw);
        Kind kind = kind(table, column);

        return switch (op) {
            case IS               -> eq(field, kind, rawValue, false);
            case IS_NOT           -> eq(field, kind, rawValue, true);
            case CONTAINS         -> like(field, rawValue, "%", "%");
            case NOT_CONTAINS     -> negate(like(field, rawValue, "%", "%"));
            case STARTS_WITH      -> like(field, rawValue, "",  "%");
            case ENDS_WITH        -> like(field, rawValue, "%", "");
            case IS_EMPTY         -> empty(field, kind);
            case IS_NOT_EMPTY     -> negate(empty(field, kind));
            case GT, GTE, LT, LTE -> compare(field, kind, rawValue, op);
            case BETWEEN          -> between(field, kind, rawValue);
            case IN               -> in(field, kind, rawValue, false);
            case NOT_IN           -> in(field, kind, rawValue, true);
        };
    }

    // ===================================================================== //
    // RÉSOLUTION DU TYPE
    // ===================================================================== //

    private Kind kind(String table, String column) {
        if (table == null || column == null) return Kind.STRING;
        String key = table.toLowerCase(Locale.ROOT) + "." + column.toLowerCase(Locale.ROOT);
        Kind k = cache.get(key);
        if (k != null) return k;
        Kind resolved = resolve(table, column);
        cache.put(key, resolved);
        return resolved;
    }

    private Kind resolve(String table, String column) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            for (String candidate : new String[]{
                    table,
                    table.toUpperCase(Locale.ROOT),
                    table.toLowerCase(Locale.ROOT)}) {

                try (ResultSet rs = meta.getColumns(null, null, candidate, null)) {
                    while (rs.next()) {
                        String name = rs.getString("COLUMN_NAME");
                        if (name == null || !name.equalsIgnoreCase(column)) continue;
                        return fromTypeName(
                                rs.getString("TYPE_NAME"),
                                rs.getInt("DATA_TYPE"));
                    }
                }
            }
        } catch (Exception ignored) { /* défaut = STRING */ }
        return Kind.STRING;
    }

    private static Kind fromTypeName(String typeName, int jdbcType) {
        if (typeName != null) {
            String t = typeName.toLowerCase(Locale.ROOT);
            if (t.contains("uuid"))                                          return Kind.UUID;
            if (t.contains("bool"))                                          return Kind.BOOLEAN;
            if (t.contains("timestamp") || t.contains("datetime"))           return Kind.DATETIME;
            if (t.contains("date"))                                          return Kind.DATE;
            if (t.contains("time"))                                          return Kind.TIME;
            if (t.contains("int") || t.contains("serial"))                   return Kind.INTEGER;
            if (t.contains("decimal") || t.contains("numeric") || t.contains("real")
                    || t.contains("double")  || t.contains("float")   || t.contains("money")) return Kind.DECIMAL;
        }
        return switch (jdbcType) {
            case Types.INTEGER, Types.SMALLINT, Types.TINYINT, Types.BIGINT -> Kind.INTEGER;
            case Types.DECIMAL, Types.NUMERIC, Types.FLOAT,
                 Types.DOUBLE, Types.REAL                                  -> Kind.DECIMAL;
            case Types.BOOLEAN, Types.BIT                                  -> Kind.BOOLEAN;
            case Types.DATE                                                -> Kind.DATE;
            case Types.TIME, Types.TIME_WITH_TIMEZONE                      -> Kind.TIME;
            case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE            -> Kind.DATETIME;
            default                                                        -> Kind.STRING;
        };
    }

    // ===================================================================== //
    // OPÉRATEURS
    // ===================================================================== //

    private Condition eq(Field<?> field, Kind kind, String rawValue, boolean negate) {
        if (rawValue == null || rawValue.isBlank()) {
            Condition c = empty(field, kind);
            return negate ? negate(c) : c;
        }
        Object v = convert(kind, rawValue);
        if (v == null) return null;
        @SuppressWarnings("unchecked") Field<Object> f = (Field<Object>) field;
        return negate ? f.ne(v) : f.eq(v);
    }

    private Condition empty(Field<?> field, Kind kind) {
        Condition isNull = field.isNull();
        if (kind == Kind.STRING) {
            @SuppressWarnings("unchecked") Field<Object> f = (Field<Object>) field;
            return isNull.or(f.eq(""));
        }
        return isNull;
    }

    private Condition like(Field<?> field, String value, String prefix, String suffix) {
        if (value == null) return null;
        String pattern = prefix
                + value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                + suffix;
        return field.like(pattern, '\\');
    }

    private Condition compare(Field<?> field, Kind kind, String rawValue, Op op) {
        Object v = convert(kind, rawValue);
        if (v == null) return null;
        @SuppressWarnings("unchecked") Field<Comparable<Object>> f = (Field<Comparable<Object>>) field;
        @SuppressWarnings("unchecked") Comparable<Object> c = (Comparable<Object>) v;
        return switch (op) {
            case GT  -> f.gt(c);
            case GTE -> f.ge(c);
            case LT  -> f.lt(c);
            case LTE -> f.le(c);
            default  -> null;
        };
    }

    private Condition between(Field<?> field, Kind kind, String rawValue) {
        if (rawValue == null) return null;
        String[] parts = rawValue.contains("..")
                ? rawValue.split("\\.\\.", 2)
                : rawValue.split(",", 2);
        if (parts.length < 2) return null;
        Object a = convert(kind, parts[0].trim());
        Object b = convert(kind, parts[1].trim());
        if (a == null || b == null) return null;
        @SuppressWarnings("unchecked") Field<Comparable<Object>> f = (Field<Comparable<Object>>) field;
        return f.between((Comparable<Object>) a, (Comparable<Object>) b);
    }

    private Condition in(Field<?> field, Kind kind, String rawValue, boolean negate) {
        if (rawValue == null || rawValue.isBlank()) return null;
        List<Object> values = new ArrayList<>();
        for (String p : rawValue.split(",")) {
            Object v = convert(kind, p.trim());
            if (v != null) values.add(v);
        }
        if (values.isEmpty()) return null;
        @SuppressWarnings("unchecked") Field<Object> f = (Field<Object>) field;
        return negate ? f.notIn(values) : f.in(values);
    }

    private Condition negate(Condition c) { return c == null ? null : DSL.not(c); }

    // ===================================================================== //
    // CONVERSION STRING → TYPE JAVA
    // ===================================================================== //

    private static Object convert(Kind kind, String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        try {
            return switch (kind) {
                case INTEGER  -> Long.parseLong(v);
                case DECIMAL  -> new BigDecimal(v);
                case BOOLEAN  -> parseBool(v);
                case DATE     -> Date.valueOf(parseDate(v));
                case TIME     -> Time.valueOf(parseTime(v));
                case DATETIME -> Timestamp.valueOf(parseDateTime(v));
                case UUID     -> UUID.fromString(v);
                case STRING   -> v;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private static Boolean parseBool(String v) {
        return switch (v.toLowerCase(Locale.ROOT)) {
            case "true",  "1", "yes", "y", "on"  -> Boolean.TRUE;
            case "false", "0", "no",  "n", "off" -> Boolean.FALSE;
            default -> null;
        };
    }

    private static LocalDate parseDate(String v) {
        for (DateTimeFormatter f : List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"))) {
            try { return LocalDate.parse(v, f); } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Bad date: " + v);
    }

    private static LocalTime parseTime(String v) {
        for (DateTimeFormatter f : List.of(
                DateTimeFormatter.ISO_LOCAL_TIME,
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm"))) {
            try { return LocalTime.parse(v, f); } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Bad time: " + v);
    }

    private static LocalDateTime parseDateTime(String v) {
        String iso = v.contains("T") ? v : v.replace(' ', 'T');
        for (DateTimeFormatter f : List.of(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))) {
            try { return LocalDateTime.parse(iso, f); } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Bad datetime: " + v);
    }
}