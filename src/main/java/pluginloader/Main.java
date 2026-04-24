package pluginloader;

import pluginloader.cli.CliParser;
import pluginloader.cli.PluginPrinter;
import pluginloader.core.LoaderVersion;
import pluginloader.core.PluginExecutor;
import pluginloader.core.PluginLoader;
import pluginloader.core.PluginRegistry;
import pluginloader.model.ExecutionResult;
import pluginloader.util.ExecutionHistory;
import pluginloader.util.JsonUtil;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            run();
        } catch (Exception e) {
            System.err.println();
            System.err.println("Fatal error: " + e.getMessage());
            System.err.println();
            System.err.println("This is a bug in the plugin loader. Please report it.");
            System.exit(1);
        }
    }

    private static void run() {
        System.out.println("=== Polyglot Plugin Loader v" + LoaderVersion.VERSION + " ===");
        System.out.println();

        PluginRegistry registry = new PluginRegistry();
        PluginLoader pluginLoader = new PluginLoader(registry);
        PluginExecutor executor = new PluginExecutor(registry);

        pluginLoader.loadAll("plugins");

        System.out.println();
        System.out.println("Loaded " + registry.size() + " plugin(s)");
        System.out.println();
        System.out.println("Type 'help' for commands, 'exit' to quit.");
        System.out.println();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] args = line.split("\\s+");

            if (isExitCommand(args)) {
                System.out.println("Bye!");
                break;
            }

            handleCommand(args, registry, executor);
        }

        scanner.close();
    }

    private static boolean isExitCommand(String[] args) {
        String cmd = args[0].toLowerCase();
        return cmd.equals("exit") || cmd.equals("quit") || cmd.equals("q");
    }

    private static void handleCommand(String[] args, PluginRegistry registry, PluginExecutor executor) {
        String cmd = args[0].toLowerCase();

        if (cmd.equals("help")) {
            printInteractiveHelp();
            return;
        }

        if (CliParser.isListCommand(args)) {
            String category = CliParser.getCategoryFilter(args);
            PluginPrinter.listPlugins(registry, category);
            return;
        }

        if (CliParser.isHistoryCommand(args)) {
            PluginPrinter.printHistory(ExecutionHistory.load());
            return;
        }

        if (CliParser.isRunPluginCommand(args)) {
            try {
                String pluginName = CliParser.getPluginName(args);
                Map<String, String> params = CliParser.parseParams(args);

                ExecutionResult result = executor.execute(pluginName, params);
                System.out.println();
                System.out.println("Result:");
                System.out.println(JsonUtil.toJson(result));
                System.out.println();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println();
            }
            return;
        }

        System.err.println("Unknown command: " + cmd);
        System.out.println("Type 'help' for available commands.");
        System.out.println();
    }

    private static void printInteractiveHelp() {
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  list                        List all plugins");
        System.out.println("  list --category=<name>      List plugins in category");
        System.out.println("  run-plugin <name> --k=v     Run a plugin with parameters");
        System.out.println("  history                     Show execution history");
        System.out.println("  help                        Show this help");
        System.out.println("  exit                        Quit the program");
        System.out.println();
    }
}
