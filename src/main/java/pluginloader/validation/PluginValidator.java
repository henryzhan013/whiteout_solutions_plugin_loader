package pluginloader.validation;

import pluginloader.core.LoaderVersion;
import pluginloader.core.Plugin;
import pluginloader.model.PluginType;
import pluginloader.util.AppLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginValidator {
    private static final AppLogger logger = new AppLogger(PluginValidator.class);

    public boolean validate(Plugin plugin) {
        List<String> errors = new ArrayList<>();

        validateName(plugin, errors);
        validateVersion(plugin, errors);
        validateLoaderCompatibility(plugin, errors);
        validateInputs(plugin, errors);
        validateOutputs(plugin, errors);

        for (String error : errors) {
            logger.error(error);
        }

        return errors.isEmpty();
    }

    private void validateName(Plugin plugin, List<String> errors) {
        try {
            String name = plugin.getName();
            if (name == null || name.isBlank()) {
                errors.add("Plugin name is null or blank");
            }
        } catch (Exception e) {
            errors.add("Exception while getting plugin name: " + e.getMessage());
        }
    }

    private void validateVersion(Plugin plugin, List<String> errors) {
        try {
            String version = plugin.getVersion();
            if (version == null || version.isBlank()) {
                errors.add("Plugin version is null or blank");
            }
        } catch (Exception e) {
            errors.add("Exception while getting plugin version: " + e.getMessage());
        }
    }

    private void validateLoaderCompatibility(Plugin plugin, List<String> errors) {
        try {
            String minVersion = plugin.getMinLoaderVersion();
            if (minVersion != null && !LoaderVersion.isCompatible(minVersion)) {
                errors.add("Plugin requires loader version " + minVersion +
                          ", but current version is " + LoaderVersion.VERSION);
            }
        } catch (Exception e) {
            errors.add("Exception while checking loader compatibility: " + e.getMessage());
        }
    }

    private void validateInputs(Plugin plugin, List<String> errors) {
        try {
            Map<String, String> inputs = plugin.getInputs();
            if (inputs == null) {
                errors.add("Plugin inputs is null");
            } else {
                validateTypeMap(inputs, "input", errors);
            }
        } catch (Exception e) {
            errors.add("Exception while getting plugin inputs: " + e.getMessage());
        }
    }

    private void validateOutputs(Plugin plugin, List<String> errors) {
        try {
            Map<String, String> outputs = plugin.getOutputs();
            if (outputs == null) {
                errors.add("Plugin outputs is null");
            } else {
                validateTypeMap(outputs, "output", errors);
            }
        } catch (Exception e) {
            errors.add("Exception while getting plugin outputs: " + e.getMessage());
        }
    }

    private void validateTypeMap(Map<String, String> typeMap, String kind, List<String> errors) {
        for (Map.Entry<String, String> entry : typeMap.entrySet()) {
            InputConstraint constraint = InputConstraint.parse(entry.getValue());
            if (!PluginType.isValid(constraint.getBaseType())) {
                errors.add("Invalid " + kind + " type '" + constraint.getBaseType() +
                          "' for parameter '" + entry.getKey() + "'");
            }
        }
    }
}
