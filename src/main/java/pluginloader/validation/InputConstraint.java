package pluginloader.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class InputConstraint {
    private final String baseType;
    private final Map<String, String> constraints;

    private InputConstraint(String baseType, Map<String, String> constraints) {
        this.baseType = baseType;
        this.constraints = constraints;
    }

    public static InputConstraint parse(String typeSpec) {
        if (typeSpec == null || typeSpec.isBlank()) {
            return new InputConstraint("string", new HashMap<>());
        }

        String[] parts = typeSpec.split(":", 2);
        String baseType = parts[0].trim().toLowerCase();
        Map<String, String> constraints = new HashMap<>();

        if (parts.length > 1) {
            String[] pairs = parts[1].split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    constraints.put(kv[0].trim().toLowerCase(), kv[1].trim());
                }
            }
        }

        return new InputConstraint(baseType, constraints);
    }

    public String getBaseType() {
        return baseType;
    }

    public boolean isRequired() {
        return !"false".equalsIgnoreCase(constraints.get("required"));
    }

    public String getDefault() {
        return constraints.get("default");
    }

    public String validate(String paramName, Object value) {
        if (value == null) {
            if (isRequired() && getDefault() == null) {
                return "Parameter '" + paramName + "' is required";
            }
            return null;
        }

        switch (baseType) {
            case "int":
            case "float":
                return validateNumber(paramName, value);
            case "string":
                return validateString(paramName, value);
            default:
                return null;
        }
    }

    private String validateNumber(String paramName, Object value) {
        double numValue;
        try {
            numValue = ((Number) value).doubleValue();
        } catch (ClassCastException e) {
            return "Parameter '" + paramName + "' must be a number";
        }

        if (constraints.containsKey("min")) {
            try {
                double min = Double.parseDouble(constraints.get("min"));
                if (numValue < min) {
                    return "Parameter '" + paramName + "' must be >= " + min;
                }
            } catch (NumberFormatException e) {
                return "Invalid 'min' constraint for parameter '" + paramName + "'";
            }
        }

        if (constraints.containsKey("max")) {
            try {
                double max = Double.parseDouble(constraints.get("max"));
                if (numValue > max) {
                    return "Parameter '" + paramName + "' must be <= " + max;
                }
            } catch (NumberFormatException e) {
                return "Invalid 'max' constraint for parameter '" + paramName + "'";
            }
        }

        return null;
    }

    private String validateString(String paramName, Object value) {
        String strValue = value.toString();

        if (constraints.containsKey("pattern")) {
            String pattern = constraints.get("pattern");
            try {
                if (!Pattern.matches(pattern, strValue)) {
                    return "Parameter '" + paramName + "' must match pattern: " + pattern;
                }
            } catch (PatternSyntaxException e) {
                return "Invalid regex pattern for parameter '" + paramName + "': " + e.getMessage();
            }
        }

        if (constraints.containsKey("minlength")) {
            try {
                int minLen = Integer.parseInt(constraints.get("minlength"));
                if (strValue.length() < minLen) {
                    return "Parameter '" + paramName + "' must be at least " + minLen + " characters";
                }
            } catch (NumberFormatException e) {
                return "Invalid 'minlength' constraint for parameter '" + paramName + "'";
            }
        }

        if (constraints.containsKey("maxlength")) {
            try {
                int maxLen = Integer.parseInt(constraints.get("maxlength"));
                if (strValue.length() > maxLen) {
                    return "Parameter '" + paramName + "' must be at most " + maxLen + " characters";
                }
            } catch (NumberFormatException e) {
                return "Invalid 'maxlength' constraint for parameter '" + paramName + "'";
            }
        }

        return null;
    }
}
