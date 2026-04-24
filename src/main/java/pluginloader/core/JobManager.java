package pluginloader.core;

import pluginloader.model.ExecutionRecord;
import pluginloader.model.ExecutionResult;
import pluginloader.model.Job;
import pluginloader.util.ExecutionHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobManager {
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final PluginExecutor pluginExecutor;

    public JobManager(PluginExecutor pluginExecutor) {
        this.pluginExecutor = pluginExecutor;
    }

    public Job submit(String pluginName, Map<String, String> inputs) {
        Job job = new Job(pluginName, inputs);
        jobs.put(job.getId(), job);

        executor.submit(() -> runJob(job));

        return job;
    }

    private void runJob(Job job) {
        job.setStatus(Job.Status.RUNNING);

        try {
            ExecutionResult result = pluginExecutor.execute(job.getPluginName(), job.getInputs());

            if (result.isSuccess()) {
                job.setStatus(Job.Status.COMPLETED);
            } else {
                job.setStatus(Job.Status.FAILED);
            }
            job.setResult(result);
        } catch (Exception e) {
            job.setStatus(Job.Status.FAILED);
            job.setResult(new ExecutionResult(job.getPluginName(), e.getMessage()));
        }

        // Save to history
        ExecutionHistory.save(new ExecutionRecord(
            job.getId(),
            job.getPluginName(),
            job.getInputs(),
            job.getResult()
        ));
    }

    public Optional<Job> getJob(String id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public List<Job> getAllJobs() {
        return new ArrayList<>(jobs.values());
    }

    public void shutdown() {
        executor.shutdown();
    }
}
