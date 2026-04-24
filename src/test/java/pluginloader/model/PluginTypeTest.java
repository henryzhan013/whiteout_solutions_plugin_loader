package pluginloader.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PluginTypeTest {

    // Coercion tests

    @Test
    void coerceStringToInt() {
        Object result = PluginType.INT.coerce("42");
        assertEquals(42, result);
    }

    @Test
    void coerceStringToFloat() {
        Object result = PluginType.FLOAT.coerce("3.14");
        assertEquals(3.14, (Double) result, 0.001);
    }

    @Test
    void coerceStringToBooleanTrue() {
        Object result = PluginType.BOOLEAN.coerce("true");
        assertEquals(true, result);
    }

    @Test
    void coerceStringToBooleanFalse() {
        Object result = PluginType.BOOLEAN.coerce("false");
        assertEquals(false, result);
    }

    @Test
    void coerceStringToString() {
        Object result = PluginType.STRING.coerce("hello");
        assertEquals("hello", result);
    }

    @Test
    void coerceInvalidIntThrows() {
        assertThrows(NumberFormatException.class, () -> {
            PluginType.INT.coerce("abc");
        });
    }

    @Test
    void coerceInvalidFloatThrows() {
        assertThrows(NumberFormatException.class, () -> {
            PluginType.FLOAT.coerce("not a number");
        });
    }

    @Test
    void coerceNegativeInt() {
        Object result = PluginType.INT.coerce("-42");
        assertEquals(-42, result);
    }

    @Test
    void coerceScientificNotation() {
        Object result = PluginType.FLOAT.coerce("1.5e2");
        assertEquals(150.0, (Double) result, 0.001);
    }

    // isValid tests

    @Test
    void isValidInt() {
        assertTrue(PluginType.isValid("int"));
    }

    @Test
    void isValidFloat() {
        assertTrue(PluginType.isValid("float"));
    }

    @Test
    void isValidString() {
        assertTrue(PluginType.isValid("string"));
    }

    @Test
    void isValidBoolean() {
        assertTrue(PluginType.isValid("boolean"));
    }

    @Test
    void isValidUnknownType() {
        assertFalse(PluginType.isValid("unknown"));
    }

    @Test
    void isValidNullDefaultsToString() {
        // null defaults to STRING type, so isValid returns true
        assertTrue(PluginType.isValid(null));
    }
}
