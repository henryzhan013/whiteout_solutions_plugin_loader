package pluginloader.core;

import pluginloader.loader.JavaPluginLoader;
import pluginloader.loader.PythonPluginLoader;
import pluginloader.util.AppLogger;
import pluginloader.validation.PluginValidator;

import java.io.File;
import java.util.List;

public class PluginLoader {
    private static final AppLogger logger = new AppLogger(PluginLoader.class);
    private final PluginRegistry registry;
    private final JavaPluginLoader javaLoader;
    private final PythonPluginLoader pythonLoader;
    private final PluginValidator validator;

    public PluginLoader(PluginRegistry registry) {
        this.registry = registry;
        this.javaLoader = new JavaPluginLoader();
        this.pythonLoader = new PythonPluginLoader();
        this.validator = new PluginValidator();
    }

    public void loadAll(String pluginsDir) {
        File dir = new File(pluginsDir);

        if (!dir.exists()) {
            logger.warn("Plugins directory does not exist: " + pluginsDir);
            return;
        }

        if (!dir.isDirectory()) {
            logger.warn("Plugins path is not a directory: " + pluginsDir);
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            logger.warn("Plugins directory is empty: " + pluginsDir);
            return;
        }

        int loadedCount = 0;

        for (File file : files) {
            loadedCount += loadFile(file);
        }

        logger.info("Loaded " + loadedCount + " plugins");
    }

    private int loadFile(File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".jar")) {
            return loadPlugins(javaLoader.load(file), file.getName());
        } else if (fileName.endsWith(".py")) {
            return loadPlugins(pythonLoader.load(file), file.getName());
        } else {
            logger.warn("Skipping unsupported file type: " + file.getName());
            return 0;
        }
    }

    private int loadPlugins(List<Plugin> plugins, String fileName) {
        int count = 0;
        for (Plugin plugin : plugins) {
            if (validator.validate(plugin)) {
                registry.register(plugin);
                count++;
            } else {
                logger.warn("Plugin validation failed: " + fileName);
            }
        }
        return count;
    }
}
