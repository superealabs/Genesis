package org.labs.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeTypeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public JsonElement serialize(LocalTime date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(date.format(this.formatter));
    }

    public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return LocalTime.parse(json.getAsString(), this.formatter);
    }
}
