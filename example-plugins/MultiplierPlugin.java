/*
 * This is an example Java plugin.
 *
 * To use it:
 * 1. Copy Plugin.java into the same folder
 * 2. Compile both files with: javac Plugin.java MultiplierPlugin.java
 * 3. Package into a JAR: jar cf multiplier-plugin.jar pluginloader/core/*.class
 * 4. Drop the JAR into the /plugins folder
 */

// package pluginloader.core;  // Remove this line if compiling standalone

import java.util.Map;

public class MultiplierPlugin implements Plugin {

    @Override
    public String getName() {
        return "multiplier";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Map<String, String> getInputs() {
        return Map.of("value", "int", "factor", "int");
    }

    @Override
    public Map<String, String> getOutputs() {
        return Map.of("result", "int");
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> inputs) {
        int value = ((Number) inputs.get("value")).intValue();
        int factor = ((Number) inputs.get("factor")).intValue();
        return Map.of("result", value * factor);
    }
}
