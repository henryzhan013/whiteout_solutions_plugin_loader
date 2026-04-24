package pluginloader.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InputConstraintTest {

    // Parsing tests

    @Test
    void parseIntType() {
        InputConstraint c = InputConstraint.parse("int");
        assertEquals("int", c.getBaseType());
    }

    @Test
    void parseIntWithConstraints() {
        InputConstraint c = InputConstraint.parse("int:min=0,max=100");
        assertEquals("int", c.getBaseType());
    }

    @Test
    void parseStringWithDefault() {
        InputConstraint c = InputConstraint.parse("string:default=hello");
        assertEquals("string", c.getBaseType());
        assertEquals("hello", c.getDefault());
    }

    @Test
    void parseEmptyDefaultsToString() {
        InputConstraint c = InputConstraint.parse("");
        assertEquals("string", c.getBaseType());
    }

    @Test
    void parseNullDefaultsToString() {
        InputConstraint c = InputConstraint.parse(null);
        assertEquals("string", c.getBaseType());
    }

    @Test
    void parseFloatWithDecimals() {
        InputConstraint c = InputConstraint.parse("float:min=0.5,max=99.9");
        assertEquals("float", c.getBaseType());
    }

    // Number validation tests

    @Test
    void validateNumberInRange() {
        InputConstraint c = InputConstraint.parse("int:min=0,max=100");
        assertNull(c.validate("value", 50));
    }

    @Test
    void validateNumberAboveMax() {
        InputConstraint c = InputConstraint.parse("int:min=0,max=100");
        String error = c.validate("value", 150);
        assertNotNull(error);
        assertTrue(error.contains("<="));
    }

    @Test
    void validateNumberBelowMin() {
        InputConstraint c = InputConstraint.parse("int:min=0,max=100");
        String error = c.validate("value", -5);
        assertNotNull(error);
        assertTrue(error.contains(">="));
    }

    @Test
    void validateNumberAtBoundary() {
        InputConstraint c = InputConstraint.parse("int:min=0,max=100");
        assertNull(c.validate("value", 0));
        assertNull(c.validate("value", 100));
    }

    // String validation tests

    @Test
    void validateStringWithinLength() {
        InputConstraint c = InputConstraint.parse("string:minlength=3,maxlength=10");
        assertNull(c.validate("value", "hello"));
    }

    @Test
    void validateStringTooShort() {
        InputConstraint c = InputConstraint.parse("string:minlength=3");
        String error = c.validate("value", "hi");
        assertNotNull(error);
        assertTrue(error.contains("at least"));
    }

    @Test
    void validateStringTooLong() {
        InputConstraint c = InputConstraint.parse("string:maxlength=5");
        String error = c.validate("value", "hello world");
        assertNotNull(error);
        assertTrue(error.contains("at most"));
    }

    @Test
    void validateStringPatternMatch() {
        InputConstraint c = InputConstraint.parse("string:pattern=[0-9]+");
        assertNull(c.validate("value", "12345"));
    }

    @Test
    void validateStringPatternNoMatch() {
        InputConstraint c = InputConstraint.parse("string:pattern=[0-9]+");
        String error = c.validate("value", "abc");
        assertNotNull(error);
        assertTrue(error.contains("pattern"));
    }

    // Edge cases

    @Test
    void invalidMinConstraintDoesNotCrash() {
        InputConstraint c = InputConstraint.parse("int:min=abc");
        String error = c.validate("value", 50);
        assertNotNull(error);
    }

    @Test
    void invalidRegexDoesNotCrash() {
        InputConstraint c = InputConstraint.parse("string:pattern=[[[");
        String error = c.validate("value", "test");
        assertNotNull(error);
    }

    @Test
    void nullValueWithRequired() {
        InputConstraint c = InputConstraint.parse("int");
        String error = c.validate("value", null);
        assertNotNull(error);
        assertTrue(error.contains("required"));
    }

    @Test
    void nullValueWithDefault() {
        InputConstraint c = InputConstraint.parse("string:default=hello");
        assertNull(c.validate("value", null));
    }
}
