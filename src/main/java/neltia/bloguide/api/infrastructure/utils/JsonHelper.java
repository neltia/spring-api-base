package neltia.bloguide.api.infrastructure.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;


public class JsonHelper {
    public static Map<String, Object> toMap(JsonObject jsonObject) {
        Map<String, Object> map = new HashMap<>();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            map.put(entry.getKey(), parseJsonElement(entry.getValue()));
        }

        return map;
    }

    private static Object parseJsonElement(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            if (jsonElement.getAsJsonPrimitive().isBoolean()) {
                return jsonElement.getAsBoolean();
            } else if (jsonElement.getAsJsonPrimitive().isNumber()) {
                return jsonElement.getAsNumber();
            } else if (jsonElement.getAsJsonPrimitive().isString()) {
                return jsonElement.getAsString();
            }
        } else if (jsonElement.isJsonObject()) {
            return toMap(jsonElement.getAsJsonObject());
        } else if (jsonElement.isJsonArray()) {
            // Convert JsonArray to List
            return jsonElement.getAsJsonArray();
        }
        return null; // Or throw an exception for unsupported types
    }
}
