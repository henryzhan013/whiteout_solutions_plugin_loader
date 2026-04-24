package pluginloader.cli;

import java.util.HashMap;
import java.util.Map;

public final class CliParser {
    private static final String RUN_PLUGIN_COMMAND = "run-plugin";
    private static final String LIST_COMMAND = "list";
    private static final String HISTORY_COMMAND = "history";

    private CliParser() {
    }

    public static boolean isRunPluginCommand(String[] args) {
        return args.length >= 2 && RUN_PLUGIN_COMMAND.equals(args[0]);
    }

    public static boolean isListCommand(String[] args) {
        return args.length >= 1 && LIST_COMMAND.equals(args[0]);
    }

    public static boolean isHistoryCommand(String[] args) {
        return args.length >= 1 && HISTORY_COMMAND.equals(args[0]);
    }

    public static String getPluginName(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Plugin name not provided");
        }
        return args[1];
    }

    public static String getCategoryFilter(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--category=")) {
                return arg.substring("--category=".length());
            }
        }
        return null;
    }

    public static Map<String, String> parseParams(String[] args) {
        Map<String, String> params = new HashMap<>();

        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--") && arg.contains("=") && !arg.startsWith("--category=")) {
                int eqIndex = arg.indexOf("=");
                String key = arg.substring(2, eqIndex);
                String value = arg.substring(eqIndex + 1);
                params.put(key, value);
            }
        }

        return params;
    }
}
