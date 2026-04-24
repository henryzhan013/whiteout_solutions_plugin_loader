package pluginloader.core;

public final class LoaderVersion {
    public static final String VERSION = "1.0.0";

    private LoaderVersion() {
    }

    public static boolean isCompatible(String minRequired) {
        if (minRequired == null || minRequired.isBlank()) {
            return true;
        }

        int[] current = parse(VERSION);
        int[] required = parse(minRequired);

        for (int i = 0; i < 3; i++) {
            if (current[i] > required[i]) {
                return true;
            }
            if (current[i] < required[i]) {
                return false;
            }
        }

        return true;
    }

    private static int[] parse(String version) {
        int[] parts = {0, 0, 0};
        String[] split = version.split("\\.");

        for (int i = 0; i < Math.min(split.length, 3); i++) {
            try {
                parts[i] = Integer.parseInt(split[i].trim());
            } catch (NumberFormatException e) {
                parts[i] = 0;
            }
        }

        return parts;
    }
}
