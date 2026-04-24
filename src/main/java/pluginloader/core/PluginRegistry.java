package pluginloader.core;

import pluginloader.util.AppLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PluginRegistry {
    private static final AppLogger logger = new AppLogger(PluginRegistry.class);
    private final Map<String, Plugin> plugins = Collections.synchronizedMap(new LinkedHashMap<>());

    public void register(Plugin plugin) {
        String key = plugin.getName().toLowerCase();
        if (plugins.containsKey(key)) {
            logger.warn("Replacing existing plugin: " + plugin.getName());
        }
        plugins.put(key, plugin);
        logger.info("Registered plugin: " + plugin.getName() + " v" + plugin.getVersion() +
                   " [" + plugin.getCategory() + "]");
    }

    public Optional<Plugin> find(String name) {
        return Optional.ofNullable(plugins.get(name.toLowerCase()));
    }

    public Collection<Plugin> all() {
        return Collections.unmodifiableCollection(plugins.values());
    }

    public List<Plugin> findByCategory(String category) {
        return plugins.values().stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<String> getCategories() {
        return plugins.values().stream()
                .map(Plugin::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public int size() {
        return plugins.size();
    }
}
