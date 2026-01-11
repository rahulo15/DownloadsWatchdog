import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final List<Path> foldersToWatch = new ArrayList<>();
    private final WatchDogService watchDogService;

    public static void main(String[] args) throws IOException, InterruptedException {
        Main app = new Main();
        app.start();
    }

    public Main() {
        this.watchDogService = new WatchDogService();

        try {
            WatchDogLogger.setup();
            System.out.println("Constructor ran successfully.");
        } catch (IOException e) {
            System.out.println("System crash: " + e.getMessage());
            throw new RuntimeException("Problems with creating the log files");
        }
    }

    private void addFolders() {
        foldersToWatch.add(Paths.get(System.getProperty("user.home"), "Downloads"));
        foldersToWatch.add(Paths.get(System.getProperty("user.home"), "Desktop"));
        foldersToWatch.add(Paths.get("U:\\"));
        foldersToWatch.add(Paths.get("O:\\"));
        LOGGER.info("Folders/Drives to Watch added.");
    }

    public void start() throws IOException, InterruptedException {
        LOGGER.info("Application Started");
        SystemTrayCreator SystemTray = new SystemTrayCreator(isPaused, foldersToWatch);
        SystemTray.SystemTrayLoader();
        addFolders();

        WatchService watchdog = FileSystems.getDefault().newWatchService();
        LOGGER.info("--- 🧹 Starting Initial Cleanup & Registration ---");
        for (Path folder : foldersToWatch) {
            if (Files.exists(folder)) {
                LOGGER.info("Processing: " + folder);
                watchDogService.performInitialCleanup(folder);
                folder.register(watchdog, StandardWatchEventKinds.ENTRY_CREATE);
            } else {
                LOGGER.warning("⚠️ Warning: Folder not found: " + folder);
            }
        }
        LOGGER.info("--- ✅ Cleanup Complete ---");
        LOGGER.info("--- 👀 Watchdog is watching " + foldersToWatch.size() + " folders ---");
        System.out.println("--- 👀 Watchdog is watching " + foldersToWatch.size() + " folders ---");
        watchDogService.startWatchDog(watchdog, isPaused);
    }
}