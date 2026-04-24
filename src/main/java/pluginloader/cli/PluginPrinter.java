package pluginloader.cli;

import pluginloader.core.LoaderVersion;
import pluginloader.core.Plugin;
import pluginloader.core.PluginRegistry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PluginPrinter {

    private PluginPrinter() {
    }

    public static void printUsage() {
        System.out.println("Polyglot Plugin Loader v" + LoaderVersion.VERSION);
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar plugin-loader.jar");
        System.out.println("      Lists all loaded plugins");
        System.out.println();
        System.out.println("  java -jar plugin-loader.jar list [--category=<name>]");
        System.out.println("      Lists plugins, optionally filtered by category");
        System.out.println();
        System.out.println("  java -jar plugin-loader.jar run-plugin <name> --key=value ...");
        System.out.println("      Executes a plugin with the given parameters");
        System.out.println();
    }

    public static void listPlugins(PluginRegistry registry, String categoryFilter) {
        if (registry.size() == 0) {
            System.out.println("No plugins loaded.");
            return;
        }

        List<Plugin> plugins;
        if (categoryFilter != null) {
            plugins = registry.findByCategory(categoryFilter);
            if (plugins.isEmpty()) {
                System.out.println("No plugins found in category: " + categoryFilter);
                System.out.println("Available categories: " + String.join(", ", registry.getCategories()));
                return;
            }
            System.out.println("Plugins in category '" + categoryFilter + "':");
        } else {
            plugins = registry.all().stream().collect(Collectors.toList());
            System.out.println("Available plugins:");
        }

        System.out.println();

        Map<String, List<Plugin>> byCategory = plugins.stream()
                .collect(Collectors.groupingBy(Plugin::getCategory));

        for (String category : byCategory.keySet().stream().sorted().collect(Collectors.toList())) {
            System.out.println("  [" + category + "]");
            for (Plugin plugin : byCategory.get(category)) {
                printPlugin(plugin);
            }
        }
    }

    private static void printPlugin(Plugin plugin) {
        System.out.println("    Name: " + plugin.getName());
        System.out.println("    Version: " + plugin.getVersion());
        System.out.println("    Inputs: " + plugin.getInputs());
        System.out.println("    Outputs: " + plugin.getOutputs());
        if (plugin.getMinLoaderVersion() != null) {
            System.out.println("    Requires loader: >=" + plugin.getMinLoaderVersion());
        }
        System.out.println();
    }
}
