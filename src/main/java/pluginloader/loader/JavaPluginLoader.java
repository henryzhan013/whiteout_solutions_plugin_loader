package pluginloader.loader;

import pluginloader.core.Plugin;
import pluginloader.util.AppLogger;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JavaPluginLoader {
    private static final AppLogger logger = new AppLogger(JavaPluginLoader.class);

    public List<Plugin> load(File jarFile) {
        List<Plugin> results = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile);
             URLClassLoader classLoader = createClassLoader(jarFile)) {

            loadPluginsFromJar(jar, classLoader, jarFile.getName(), results);

        } catch (Exception e) {
            logger.error("Failed to load JAR file: " + jarFile.getName(), e);
        }

        if (results.isEmpty()) {
            logger.warn("No plugins found in JAR: " + jarFile.getName());
        }

        return results;
    }

    private URLClassLoader createClassLoader(File jarFile) throws Exception {
        return new URLClassLoader(
            new URL[]{jarFile.toURI().toURL()},
            Thread.currentThread().getContextClassLoader()
        );
    }

    private void loadPluginsFromJar(JarFile jar, URLClassLoader classLoader, String jarName, List<Plugin> results) {
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            Optional<String> className = extractClassName(entry);

            if (className.isEmpty()) {
                continue;
            }

            Optional<Plugin> plugin = loadPluginClass(classLoader, className.get(), jarName);
            plugin.ifPresent(results::add);
        }
    }

    private Optional<String> extractClassName(JarEntry entry) {
        String entryName = entry.getName();

        if (!entryName.endsWith(".class")) {
            return Optional.empty();
        }
        if (entryName.contains("$")) {
            return Optional.empty();
        }

        String className = entryName
                .replace("/", ".")
                .replace(".class", "");

        return Optional.of(className);
    }

    private Optional<Plugin> loadPluginClass(URLClassLoader classLoader, String className, String jarName) {
        try {
            Class<?> clazz = classLoader.loadClass(className);

            if (!isValidPluginClass(clazz)) {
                return Optional.empty();
            }

            Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();
            logger.info("Loaded Java plugin: " + plugin.getName() + " from " + jarName);

            return Optional.of(plugin);

        } catch (NoSuchMethodException e) {
            logger.warn("No default constructor for class: " + className);
        } catch (ClassNotFoundException e) {
            logger.warn("Class not found: " + className);
        } catch (LinkageError e) {
            logger.warn("Linkage error for class " + className + ": " + e.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to instantiate class " + className + ": " + e.getMessage());
        }

        return Optional.empty();
    }

    private boolean isValidPluginClass(Class<?> clazz) {
        return Plugin.class.isAssignableFrom(clazz)
                && !clazz.isInterface()
                && !Modifier.isAbstract(clazz.getModifiers());
    }
}
