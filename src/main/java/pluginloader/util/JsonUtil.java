package pluginloader.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private JsonUtil() {
    }

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static Map<String, Object> fromJsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }

        try {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> result = GSON.fromJson(json, type);
            return result != null ? result : Collections.emptyMap();
        } catch (JsonSyntaxException e) {
            return Map.of("error", "Invalid JSON: " + e.getMessage());
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return GSON.fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Invalid JSON: " + e.getMessage(), e);
        }
    }
}
