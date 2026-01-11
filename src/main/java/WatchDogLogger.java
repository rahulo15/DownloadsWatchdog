import java.io.IOException;
import java.util.logging.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WatchDogLogger {
    public WatchDogLogger() {}
    // We make this static so we can call it without creating an object
    public static void setup() throws IOException {
        // suppress the logging output to the console
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        // Set the Level on the ROOT
        rootLogger.setLevel(Level.INFO);

        // Create the file handler (append = true)
        FileHandler fileTxt = new FileHandler("watchdog.log", 1024 * 1024, 10,true);

        // Create a simple formatter
        // SimpleFormatter formatterTxt = new SimpleFormatter();
        // fileTxt.setFormatter(formatterTxt);

        //Complex formatter for multithreading
        fileTxt.setFormatter(new Formatter() {
            // Create a date format once
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public String format(LogRecord record) {
                // Get the current thread's name
                String threadName = Thread.currentThread().getName();

                // Get the log level (INFO, SEVERE, etc)
                String level = record.getLevel().getLocalizedName();

                // Get the actual message you typed
                String message = formatMessage(record);

                // Get the timestamp
                String timestamp = dateFormat.format(new Date(record.getMillis()));

                // Combine them into one neat line
                // Format: [Time] [Thread] [Level]: Message (New Line)
                return String.format("[%s] [%-20s] [%-10s]: %s%n",
                        timestamp, threadName, level, message);
            }
        });

        // Add the handler to the logger
        rootLogger.addHandler(fileTxt);
    }
}