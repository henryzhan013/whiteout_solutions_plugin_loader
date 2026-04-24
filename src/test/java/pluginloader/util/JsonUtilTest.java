package pluginloader.util;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void toJsonSimpleMap() {
        Map<String, Object> map = Map.of("a", 1, "b", "hello");
        String json = JsonUtil.toJson(map);
        assertNotNull(json);
        assertTrue(json.contains("\"a\""));
        assertTrue(json.contains("\"b\""));
    }

    @Test
    void fromJsonToMapValid() {
        String json = "{\"a\": 1, \"b\": \"hello\"}";
        Map<String, Object> result = JsonUtil.fromJsonToMap(json);
        assertEquals(1.0, result.get("a")); // Gson parses numbers as doubles
        assertEquals("hello", result.get("b"));
    }

    @Test
    void fromJsonToMapInvalidJson() {
        String json = "not valid json";
        Map<String, Object> result = JsonUtil.fromJsonToMap(json);
        assertTrue(result.containsKey("error"));
    }

    @Test
    void fromJsonToMapEmptyString() {
        Map<String, Object> result = JsonUtil.fromJsonToMap("");
        assertTrue(result.isEmpty());
    }

    @Test
    void fromJsonToMapNull() {
        Map<String, Object> result = JsonUtil.fromJsonToMap(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void fromJsonToMapEmptyObject() {
        Map<String, Object> result = JsonUtil.fromJsonToMap("{}");
        assertTrue(result.isEmpty());
    }

    @Test
    void roundTrip() {
        Map<String, Object> original = Map.of("name", "test", "value", 42);
        String json = JsonUtil.toJson(original);
        Map<String, Object> parsed = JsonUtil.fromJsonToMap(json);
        assertEquals("test", parsed.get("name"));
    }

    @Test
    void fromJsonToClass() {
        String json = "{\"name\": \"test\", \"version\": \"1.0\"}";
        TestPojo result = JsonUtil.fromJson(json, TestPojo.class);
        assertEquals("test", result.name);
        assertEquals("1.0", result.version);
    }

    @Test
    void fromJsonToClassInvalid() {
        String json = "not json";
        assertThrows(IllegalArgumentException.class, () -> {
            JsonUtil.fromJson(json, TestPojo.class);
        });
    }

    static class TestPojo {
        String name;
        String version;
    }
}
