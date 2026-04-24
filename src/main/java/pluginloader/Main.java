package pluginloader;

import pluginloader.cli.CliParser;
import pluginloader.cli.PluginPrinter;
import pluginloader.core.JobManager;
import pluginloader.core.LoaderVersion;
import pluginloader.core.PluginExecutor;
import pluginloader.core.PluginLoader;
import pluginloader.core.PluginRegistry;
import pluginloader.model.Job;
import pluginloader.util.ExecutionHistory;

import java.util.Map;
import java.util.Optional;
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
        JobManager jobManager = new JobManager(executor);

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

            handleCommand(args, registry, jobManager);
        }

        scanner.close();
        jobManager.shutdown();
    }

    private static boolean isExitCommand(String[] args) {
        String cmd = args[0].toLowerCase();
        return cmd.equals("exit") || cmd.equals("quit") || cmd.equals("q");
    }

    private static void handleCommand(String[] args, PluginRegistry registry, JobManager jobManager) {
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

        if (CliParser.isJobsCommand(args)) {
            PluginPrinter.printJobs(jobManager.getAllJobs());
            return;
        }

        if (CliParser.isJobCommand(args)) {
            try {
                String jobId = CliParser.getJobId(args);
                Optional<Job> job = jobManager.getJob(jobId);
                if (job.isPresent()) {
                    PluginPrinter.printJob(job.get());
                } else {
                    System.out.println("Job not found: " + jobId);
                    System.out.println();
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println();
            }
            return;
        }

        if (CliParser.isRunPluginCommand(args)) {
            try {
                String pluginName = CliParser.getPluginName(args);
                Map<String, String> params = CliParser.parseParams(args);

                Job job = jobManager.submit(pluginName, params);
                System.out.println("Job submitted: " + job.getId());
                System.out.println("Use 'job " + job.getId() + "' to check status");
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
        System.out.println("  run-plugin <name> --k=v     Submit a plugin job");
        System.out.println("  jobs                        List all jobs");
        System.out.println("  job <id>                    Get job status/result");
        System.out.println("  history                     Show execution history");
        System.out.println("  help                        Show this help");
        System.out.println("  exit                        Quit the program");
        System.out.println();
    }
}
