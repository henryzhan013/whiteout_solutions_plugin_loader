package pluginloader.model;

import java.util.Map;

public class ExecutionResult {

    public enum Status {
        SUCCESS,
        ERROR
    }

    private final Status status;
    private final String pluginName;
    private final String pluginVersion;
    private final Map<String, Object> outputs;
    private final String errorMessage;
    private final long executionTimeMs;

    public ExecutionResult(String pluginName, String pluginVersion, Map<String, Object> outputs, long executionTimeMs) {
        this.status = Status.SUCCESS;
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
        this.outputs = outputs;
        this.errorMessage = null;
        this.executionTimeMs = executionTimeMs;
    }

    public ExecutionResult(String pluginName, String errorMessage) {
        this.status = Status.ERROR;
        this.pluginName = pluginName;
        this.pluginVersion = null;
        this.outputs = null;
        this.errorMessage = errorMessage;
        this.executionTimeMs = 0;
    }

    public Status getStatus() {
        return status;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public Map<String, Object> getOutputs() {
        return outputs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
