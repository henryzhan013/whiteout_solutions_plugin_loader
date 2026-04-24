package pluginloader.loader;

import pluginloader.core.Plugin;
import pluginloader.model.PluginMetadata;
import pluginloader.util.JsonUtil;
import pluginloader.util.PythonRunner;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class PythonPlugin implements Plugin {
    private final File pythonFile;
    private final PluginMetadata metadata;

    public PythonPlugin(File pythonFile, PluginMetadata metadata) {
        this.pythonFile = pythonFile;
        this.metadata = metadata;
    }

    @Override
    public String getName() {
        return metadata.getName();
    }

    @Override
    public String getVersion() {
        return metadata.getVersion();
    }

    @Override
    public Map<String, String> getInputs() {
        return metadata.getInputs();
    }

    @Override
    public Map<String, String> getOutputs() {
        return metadata.getOutputs();
    }

    @Override
    public String getCategory() {
        return metadata.getCategory();
    }

    @Override
    public String getMinLoaderVersion() {
        return metadata.getMinLoaderVersion();
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> inputs) {
        String jsonInputs = JsonUtil.toJson(inputs);

        try {
            String stdout = PythonRunner.execute("execute", pythonFile, jsonInputs);
            Map<String, Object> result = JsonUtil.fromJsonToMap(stdout);

            if (result.containsKey("error")) {
                throw new RuntimeException("Python plugin error: " + result.get("error"));
            }

            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to run Python plugin", e);
        }
    }
}
