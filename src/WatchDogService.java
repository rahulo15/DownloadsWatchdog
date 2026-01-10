import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class WatchDogService {
    private static final Logger LOGGER = Logger.getLogger(WatchDogService.class.getName());
    private final FileProcessor fileProcessor;

    public WatchDogService() {
        this.fileProcessor = new FileProcessor();
    }

    public void startWatchDog(WatchService watchdog, AtomicBoolean isPaused) throws InterruptedException {
        //runs indefinitely
        while (true) {
            WatchKey key = watchdog.take();
            if (isPaused.get()) {
                LOGGER.info("Event detected but ignored (Paused)...");
                // IMPORTANT: You must still poll events and reset the key, otherwise the OS buffer will fill up or the key stays invalid.
                key.pollEvents();
                boolean valid = key.reset();
                if (!valid) break;
                continue; // Skip the actual processing logic below
            }
            Path currentDir = (Path) key.watchable();
            for (WatchEvent<?> event : key.pollEvents()) {
                // FIX START: Check for the "Overflow" event
                if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                    continue; // Skip this iteration, it's just noise
                }
                Path fileName = (Path) event.context();
                // EXTRA SAFETY: Just in case context is still null
                if (fileName == null) {
                    continue;
                }
                Path fullPath = currentDir.resolve(fileName);
                fileProcessor.processFile(fullPath, currentDir, true);
            }
            if (!key.reset()) break;
        }
    }

    public void performInitialCleanup(Path folder) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {
                fileProcessor.processFile(file, folder, false);
            }
        } catch (IOException e) {
            LOGGER.severe("Error during cleanup: " + folder + ": " + e.getMessage());
        }
    }
}
