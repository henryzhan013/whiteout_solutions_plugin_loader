package pluginloader.core;

import pluginloader.model.ExecutionRecord;
import pluginloader.model.ExecutionResult;
import pluginloader.model.PluginType;
import pluginloader.util.AppLogger;
import pluginloader.util.ExecutionHistory;
import pluginloader.validation.InputConstraint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PluginExecutor {
    private static final AppLogger logger = new AppLogger(PluginExecutor.class);
    private final PluginRegistry registry;

    public PluginExecutor(PluginRegistry registry) {
        this.registry = registry;
    }

    public ExecutionResult execute(String pluginName, Map<String, String> rawParams) {
        Optional<Plugin> pluginOpt = registry.find(pluginName);

        if (pluginOpt.isEmpty()) {
            return new ExecutionResult(pluginName, "Plugin not found: " + pluginName);
        }

        Plugin plugin = pluginOpt.get();

        Map<String, Object> coercedInputs;
        try {
            coercedInputs = coerceInputs(plugin, rawParams);
        } catch (IllegalArgumentException e) {
            return new ExecutionResult(pluginName, e.getMessage());
        }

        List<String> validationErrors = validateInputConstraints(plugin, coercedInputs);
        if (!validationErrors.isEmpty()) {
            ExecutionResult result = new ExecutionResult(pluginName, String.join("; ", validationErrors));
            ExecutionHistory.save(new ExecutionRecord(pluginName, rawParams, result));
            return result;
        }

        ExecutionResult result = executePlugin(plugin, coercedInputs);
        ExecutionHistory.save(new ExecutionRecord(pluginName, rawParams, result));
        return result;
    }

    private Map<String, Object> coerceInputs(Plugin plugin, Map<String, String> rawParams) {
        Map<String, String> inputTypes = plugin.getInputs();
        Map<String, Object> coercedInputs = new HashMap<>();

        for (Map.Entry<String, String> inputDef : inputTypes.entrySet()) {
            String paramName = inputDef.getKey();
            InputConstraint constraint = InputConstraint.parse(inputDef.getValue());

            String rawValue = rawParams.get(paramName);

            if (rawValue == null) {
                String defaultValue = constraint.getDefault();
                if (defaultValue != null) {
                    rawValue = defaultValue;
                } else if (constraint.isRequired()) {
                    throw new IllegalArgumentException("Missing required input: " + paramName);
                } else {
                    continue;
                }
            }

            PluginType type = PluginType.fromString(constraint.getBaseType());
            if (type == null) {
                type = PluginType.STRING;
            }

            try {
                Object coercedValue = type.coerce(rawValue);
                coercedInputs.put(paramName, coercedValue);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Failed to coerce parameter '" + paramName + "' to type '" + type.getTypeName() + "': " + e.getMessage());
            }
        }

        return coercedInputs;
    }

    private List<String> validateInputConstraints(Plugin plugin, Map<String, Object> inputs) {
        List<String> errors = new ArrayList<>();
        Map<String, String> inputTypes = plugin.getInputs();

        for (Map.Entry<String, String> inputDef : inputTypes.entrySet()) {
            String paramName = inputDef.getKey();
            InputConstraint constraint = InputConstraint.parse(inputDef.getValue());
            Object value = inputs.get(paramName);

            String error = constraint.validate(paramName, value);
            if (error != null) {
                errors.add(error);
            }
        }

        return errors;
    }

    private ExecutionResult executePlugin(Plugin plugin, Map<String, Object> inputs) {
        logger.info("Executing plugin: " + plugin.getName());
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> outputs = plugin.execute(inputs);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Execution completed in " + duration + "ms");

            return new ExecutionResult(plugin.getName(), plugin.getVersion(), outputs, duration);
        } catch (Exception e) {
            logger.error("Execution failed for plugin: " + plugin.getName(), e);
            return new ExecutionResult(plugin.getName(), e.getMessage());
        }
    }
}
