package pluginloader.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

public final class PythonRunner {
    private static final int TIMEOUT_SECONDS = 30;
    private static String cachedRunnerPath;

    private PythonRunner() {
    }

    public static String getRunnerPath() {
        if (cachedRunnerPath != null) {
            return cachedRunnerPath;
        }
        cachedRunnerPath = resolveRunnerPath();
        return cachedRunnerPath;
    }

    public static String execute(String action, File pluginFile, String... extraArgs) throws IOException, InterruptedException {
        ProcessBuilder pb;
        String runnerPath = getRunnerPath();

        if (extraArgs.length > 0) {
            pb = new ProcessBuilder("python", runnerPath, pluginFile.getAbsolutePath(), action, extraArgs[0]);
        } else {
            pb = new ProcessBuilder("python", runnerPath, pluginFile.getAbsolutePath(), action);
        }
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try {
            StringBuilder stdout = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stdout.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IOException("Python process timed out after " + TIMEOUT_SECONDS + " seconds");
            }

            int exitCode = process.exitValue();
            String output = stdout.toString().trim();

            if (exitCode != 0) {
                throw new IOException("Python process exited with code " + exitCode + ": " + output);
            }

            return output;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    private static String resolveRunnerPath() {
        try {
            String jarPath = PythonRunner.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath();
            File jarFile = new File(jarPath);
            File runnerFile = new File(jarFile.getParentFile(), "runner.py");

            if (runnerFile.exists()) {
                return runnerFile.getAbsolutePath();
            }

            try (InputStream is = PythonRunner.class.getClassLoader().getResourceAsStream("runner.py")) {
                if (is != null) {
                    Path tempFile = Files.createTempFile("runner", ".py");
                    Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    tempFile.toFile().deleteOnExit();
                    return tempFile.toAbsolutePath().toString();
                }
            }

            throw new RuntimeException("Cannot find runner.py");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve runner.py path", e);
        }
    }
}
