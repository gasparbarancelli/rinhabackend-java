package com.gasparbarancelli.rinhabackend;

import com.google.gson.*;

import java.lang.reflect.Type;

final class StringValueAdapter implements JsonSerializer<String>, JsonDeserializer<String> {

    @Override
    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src);
    }

    @Override
    public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json != null
                && json.isJsonPrimitive()
                && json.getAsJsonPrimitive().isString()
                && !json.getAsString().isBlank()) {
            return json.getAsString();
        } else {
            throw new JsonParseException("Expected a string but found " + json);
        }
    }
}