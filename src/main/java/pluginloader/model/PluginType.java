package pluginloader.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum PluginType {
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    BOOLEAN("boolean");

    private final String typeName;

    PluginType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public static PluginType fromString(String s) {
        if (s == null) {
            return STRING;
        }
        for (PluginType type : values()) {
            if (type.typeName.equalsIgnoreCase(s)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isValid(String s) {
        return fromString(s) != null;
    }

    public static Set<String> validTypeNames() {
        return Arrays.stream(values())
                .map(PluginType::getTypeName)
                .collect(Collectors.toSet());
    }

    public Object coerce(String rawValue) {
        switch (this) {
            case INT:
                return Integer.parseInt(rawValue);
            case FLOAT:
                return Double.parseDouble(rawValue);
            case BOOLEAN:
                return Boolean.parseBoolean(rawValue);
            case STRING:
            default:
                return rawValue;
        }
    }
}
