import java.io.IOException;
import java.util.logging.*;

public class WatchdogLogger {
    // We make this static so we can call it without creating an object
    public static void setup() throws IOException {

        // Get the global logger to configure it
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        // suppress the logging output to the console
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        logger.setLevel(Level.INFO);

        // Create the file handler (append = true)
        FileHandler fileTxt = new FileHandler("watchdog.log", 1024 * 1024, 10,true);

        // Create a simple formatter
        SimpleFormatter formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);

        // Add the handler to the logger
        logger.addHandler(fileTxt);
    }
}