package pluginloader.model;

import java.util.Map;

public final class PluginMetadata {
    private final String name;
    private final String version;
    private final Map<String, String> inputs;
    private final Map<String, String> outputs;
    private final String category;
    private final String minLoaderVersion;

    public PluginMetadata(String name, String version, Map<String, String> inputs, Map<String, String> outputs) {
        this(name, version, inputs, outputs, null, null);
    }

    public PluginMetadata(String name, String version, Map<String, String> inputs, Map<String, String> outputs,
                          String category, String minLoaderVersion) {
        this.name = name;
        this.version = version;
        this.inputs = inputs;
        this.outputs = outputs;
        this.category = category;
        this.minLoaderVersion = minLoaderVersion;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public Map<String, String> getOutputs() {
        return outputs;
    }

    public String getCategory() {
        return category != null ? category : "uncategorized";
    }

    public String getMinLoaderVersion() {
        return minLoaderVersion;
    }

    @Override
    public String toString() {
        return "PluginMetadata{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                ", category='" + category + '\'' +
                ", minLoaderVersion='" + minLoaderVersion + '\'' +
                '}';
    }
}
