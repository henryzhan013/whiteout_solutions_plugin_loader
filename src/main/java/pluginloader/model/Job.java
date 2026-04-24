package pluginloader.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class Job {
    public enum Status {
        PENDING, RUNNING, COMPLETED, FAILED
    }

    private final String id;
    private final String pluginName;
    private final Map<String, String> inputs;
    private final String createdAt;
    private volatile Status status;
    private volatile ExecutionResult result;
    private volatile String completedAt;

    public Job(String pluginName, Map<String, String> inputs) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.pluginName = pluginName;
        this.inputs = inputs;
        this.createdAt = Instant.now().toString();
        this.status = Status.PENDING;
    }

    public String getId() {
        return id;
    }

    public String getPluginName() {
        return pluginName;
    }

    public Map<String, String> getInputs() {
        return inputs;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public ExecutionResult getResult() {
        return result;
    }

    public void setResult(ExecutionResult result) {
        this.result = result;
        this.completedAt = Instant.now().toString();
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public boolean isDone() {
        return status == Status.COMPLETED || status == Status.FAILED;
    }
}
