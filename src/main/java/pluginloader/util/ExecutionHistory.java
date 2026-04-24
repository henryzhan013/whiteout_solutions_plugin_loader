package pluginloader.util;

import pluginloader.model.ExecutionRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class ExecutionHistory {
    private static final Path HISTORY_FILE = Path.of("executions.json");

    private ExecutionHistory() {
    }

    public static void save(ExecutionRecord record) {
        try {
            String json = JsonUtil.toJsonCompact(record);
            Files.writeString(
                HISTORY_FILE,
                json + "\n",
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Warning: Could not save execution history: " + e.getMessage());
        }
    }

    public static List<ExecutionRecord> load() {
        List<ExecutionRecord> records = new ArrayList<>();

        if (!Files.exists(HISTORY_FILE)) {
            return records;
        }

        try {
            List<String> lines = Files.readAllLines(HISTORY_FILE);
            for (String line : lines) {
                if (line.isBlank()) continue;
                try {
                    ExecutionRecord record = JsonUtil.fromJson(line, ExecutionRecord.class);
                    if (record != null) {
                        records.add(record);
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not read execution history: " + e.getMessage());
        }

        return records;
    }

    public static void clear() {
        try {
            Files.deleteIfExists(HISTORY_FILE);
        } catch (IOException e) {
            System.err.println("Warning: Could not clear execution history: " + e.getMessage());
        }
    }
}
