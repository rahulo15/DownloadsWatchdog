import java.io.IOException;
import java.util.logging.*;

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
        SimpleFormatter formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);

        // Add the handler to the logger
        rootLogger.addHandler(fileTxt);
    }
}