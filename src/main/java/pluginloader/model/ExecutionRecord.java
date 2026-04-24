package pluginloader.model;

import java.time.Instant;
import java.util.Map;

public class ExecutionRecord {
    private final String timestamp;
    private final String plugin;
    private final Map<String, String> inputs;
    private final String status;
    private final Map<String, Object> output;
    private final String error;

    public ExecutionRecord(String plugin, Map<String, String> inputs, ExecutionResult result) {
        this.timestamp = Instant.now().toString();
        this.plugin = plugin;
        this.inputs = inputs;
        this.status = result.getStatus().name().toLowerCase();
        this.output = result.getOutputs();
        this.error = result.getErrorMessage();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPlugin() {
        return plugin;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public String getStatus() {
        return status;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public String getError() {
        return error;
    }
}
