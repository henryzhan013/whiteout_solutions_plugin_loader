package pluginloader.loader;

import pluginloader.core.Plugin;
import pluginloader.model.PluginMetadata;
import pluginloader.util.AppLogger;
import pluginloader.util.JsonUtil;
import pluginloader.util.PythonRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PythonPluginLoader {
    private static final AppLogger logger = new AppLogger(PythonPluginLoader.class);

    public List<Plugin> load(File pyFile) {
        List<Plugin> results = new ArrayList<>();

        try {
            String metadataJson = PythonRunner.execute("metadata", pyFile);

            Map<String, Object> jsonMap = JsonUtil.fromJsonToMap(metadataJson);
            if (jsonMap.containsKey("error")) {
                logger.error("Error loading Python plugin " + pyFile.getName() + ": " + jsonMap.get("error"));
                return results;
            }

            PluginMetadata metadata = JsonUtil.fromJson(metadataJson, PluginMetadata.class);
            logger.info("Loaded Python plugin metadata: " + metadata.getName() + " v" + metadata.getVersion());

            results.add(new PythonPlugin(pyFile, metadata));
        } catch (Exception e) {
            logger.error("Failed to load Python plugin: " + pyFile.getName(), e);
        }

        return results;
    }
}
