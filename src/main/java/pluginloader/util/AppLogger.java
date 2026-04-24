package pluginloader.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppLogger {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String context;

    public AppLogger(Class<?> clazz) {
        this.context = clazz.getSimpleName();
    }

    public void info(String message) {
        System.out.println(formatMessage("INFO", message));
    }

    public void warn(String message) {
        System.out.println(formatMessage("WARN", message));
    }

    public void error(String message) {
        System.err.println(formatMessage("ERROR", message));
    }

    public void error(String message, Throwable throwable) {
        System.err.println(formatMessage("ERROR", message));
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }

    private String formatMessage(String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return "[" + timestamp + "] [" + level + "] [" + context + "] " + message;
    }
}
