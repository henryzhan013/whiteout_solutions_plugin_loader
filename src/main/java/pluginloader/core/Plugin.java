package pluginloader.core;

import java.util.Map;

public interface Plugin {
    String getName();
    String getVersion();
    Map<String, String> getInputs();
    Map<String, String> getOutputs();
    Map<String, Object> execute(Map<String, Object> inputs);

    default String getCategory() {
        return "uncategorized";
    }

    default String getMinLoaderVersion() {
        return null;
    }
}
