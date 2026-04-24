package pluginloader;

import pluginloader.cli.CliParser;
import pluginloader.cli.PluginPrinter;
import pluginloader.core.LoaderVersion;
import pluginloader.core.PluginExecutor;
import pluginloader.core.PluginLoader;
import pluginloader.core.PluginRegistry;
import pluginloader.model.ExecutionResult;
import pluginloader.util.JsonUtil;

import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println();
            System.err.println("Fatal error: " + e.getMessage());
            System.err.println();
            System.err.println("This is a bug in the plugin loader. Please report it.");
            System.exit(1);
        }
    }

    private static void run(String[] args) {
        System.out.println("=== Polyglot Plugin Loader v" + LoaderVersion.VERSION + " ===");
        System.out.println();

        PluginRegistry registry = new PluginRegistry();
        PluginLoader pluginLoader = new PluginLoader(registry);
        PluginExecutor executor = new PluginExecutor(registry);

        pluginLoader.loadAll("plugins");

        System.out.println();
        System.out.println("Loaded " + registry.size() + " plugin(s)");
        System.out.println();

        if (args.length == 0) {
            PluginPrinter.printUsage();
            PluginPrinter.listPlugins(registry, null);
            return;
        }

        if (CliParser.isListCommand(args)) {
            String category = CliParser.getCategoryFilter(args);
            PluginPrinter.listPlugins(registry, category);
            return;
        }

        if (!CliParser.isRunPluginCommand(args)) {
            System.err.println("Error: Unknown command. Expected 'run-plugin' or 'list'");
            PluginPrinter.printUsage();
            return;
        }

        String pluginName = CliParser.getPluginName(args);
        Map<String, String> params = CliParser.parseParams(args);

        ExecutionResult result = executor.execute(pluginName, params);
        System.out.println();
        System.out.println("Result:");
        System.out.println(JsonUtil.toJson(result));
    }
}
