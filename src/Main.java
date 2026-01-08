import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Main {
    private static final AtomicBoolean isPaused = new AtomicBoolean(false);
    private static final List<Path> foldersToWatch = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) throws IOException, InterruptedException {
        try {
            WatchdogLogger.setup();
        } catch (IOException e) {
            System.out.println("System crash: " + e.getMessage());
            throw new RuntimeException("Problems with creating the log files");
        }
        LOGGER.info("Application Started");
        SystemTrayLoader();

        foldersToWatch.add(Paths.get(System.getProperty("user.home"), "Downloads"));
        foldersToWatch.add(Paths.get(System.getProperty("user.home"), "Desktop"));
        foldersToWatch.add(Paths.get("U:\\"));
        foldersToWatch.add(Paths.get("O:\\"));
        LOGGER.info("Folders/Drives to Watch added.");

        WatchService watchdog = FileSystems.getDefault().newWatchService();
        LOGGER.info("--- 🧹 Starting Initial Cleanup & Registration ---");
        for (Path folder : foldersToWatch) {
            if (Files.exists(folder)) {
                LOGGER.info("Processing: " + folder);
                performInitialCleanup(folder);
                folder.register(watchdog, StandardWatchEventKinds.ENTRY_CREATE);
            } else {
                LOGGER.warning("⚠️ Warning: Folder not found: " + folder);
            }
        }
        LOGGER.info("--- ✅ Cleanup Complete ---");
        LOGGER.info("--- 👀 Watchdog is watching " + foldersToWatch.size() + " folders ---");
        System.out.println("--- 👀 Watchdog is watching " + foldersToWatch.size() + " folders ---");
        startWatchDog(watchdog);
    }

    private static void performInitialCleanup(Path folder) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {
                processFile(file, folder, false);
            }
        } catch (IOException e) {
            LOGGER.severe("Error during cleanup: " + folder + ": " + e.getMessage());
        }
    }

    private static void startWatchDog(WatchService watchdog) throws IOException, InterruptedException {
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
                processFile(fullPath, currentDir, true);
            }
            if (!key.reset()) break;
        }
    }

    private static void processFile(Path file, Path parentFolder, boolean requiresDelay) {
        String fileNameStr = file.getFileName().toString().toLowerCase();
        if (Files.isDirectory(file)) return;
        String targetFolderName = getCategory(fileNameStr);
        if(targetFolderName.equals("Other")) return;
        Path targetDir = parentFolder.resolve(targetFolderName);
        Path originalDestination = targetDir.resolve(file.getFileName());
        Path destination = getUniqueDestination(originalDestination);

        try {
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }
            if(requiresDelay) Thread.sleep(1000);
            Files.move(file, destination, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("✅ Moved " + file.getFileName() + " -> " + targetFolderName);
            System.out.println("✅ Moved " + file.getFileName() + " -> " + targetFolderName);
        } catch (Exception e) {
            System.out.println("❌ Failed to move file: " + file.getFileName());
            LOGGER.warning("❌ Failed to move file: " + file.getFileName());
        }
    }

    // --- HELPER: Handle Duplicate Files ---
    private static Path getUniqueDestination(Path target) {
        // If the file doesn't exist yet, we are good to go!
        if (!Files.exists(target)) {
            return target;
        }

        String fileName = target.getFileName().toString();
        String name = "";
        String ext = "";

        // Find the dot to split name and extension
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex > 0) {
            name = fileName.substring(0, dotIndex); // "Resume"
            ext = fileName.substring(dotIndex);     // ".pdf"
        } else {
            name = fileName; // File has no extension (rare but possible)
        }

        int counter = 1;
        while (true) {
            // Create new name: "Resume (1).pdf"
            String newName = name + " (" + counter + ")" + ext;
            Path newPath = target.resolveSibling(newName);

            // If this new name is free, use it!
            if (!Files.exists(newPath)) {
                return newPath;
            }
            counter++;
        }
    }

    // --- MAIN SORTING LOGIC ---
    private static String getCategory(String filename) {
        // 1. IMAGES
        if (isOneOf(filename, ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp", ".ico", ".tiff")) {
            return "Images";
        }

        // 2. VIDEO
        else if (isOneOf(filename, ".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".webm", ".m4v")) {
            return "Video";
        }

        // 3. AUDIO
        else if (isOneOf(filename, ".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a")) {
            return "Music";
        }

        // 4. DOCUMENTS (General)
        else if (isOneOf(filename, ".pdf", ".doc", ".docx", ".txt", ".rtf", ".odt", ".md", ".epub")) {
            return "Documents";
        }

        // 5. SPREADSHEETS
        else if (isOneOf(filename, ".xls", ".xlsx", ".csv", ".ods", ".numbers")) {
            return "Spreadsheets";
        }

        // 6. PRESENTATIONS
        else if (isOneOf(filename, ".ppt", ".pptx", ".odp", ".key")) {
            return "Presentations";
        }

        // 7. ARCHIVES / COMPRESSED
        else if (isOneOf(filename, ".zip", ".rar", ".7z", ".tar", ".gz", ".iso", ".dmg")) {
            return "Archives";
        }

        // 8. EXECUTABLES / INSTALLERS
        else if (isOneOf(filename, ".exe", ".msi", ".pkg", ".deb", ".rpm", ".bat", ".sh", ".apk")) {
            return "Installers";
        }

        // 9. CODE / DEVELOPER FILES (Specific to you!)
        else if (isOneOf(filename, ".java", ".py", ".js", ".html", ".css", ".cpp", ".c", ".json", ".xml", ".sql")) {
            return "Code";
        }

        return "Other";
    }

    // --- HELPER TOOL ---
    // This allows us to check many extensions at once without repeating code
    private static boolean isOneOf(String filename, String... extensions) {
        for (String ext : extensions) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private static void SystemTrayLoader() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray not supported!");
            return;
        }

        // 2. Get the system tray instance
        final SystemTray tray = SystemTray.getSystemTray();

        Image image = GenerateImage();

        // 4. Create a Popup Menu (Right-click menu)
        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");

        // Add functionality to menu items
        exitItem.addActionListener(e -> {
            LOGGER.info("Service has been stopped.");
            System.exit(0);
        });

        popup.add(exitItem);

        // 5. Create the TrayIcon
        TrayIcon trayIcon = new TrayIcon(image, "DownloadsWatchdog", popup);
        trayIcon.setImageAutoSize(true);

        class CleanupHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newState = !isPaused.get();
                isPaused.set(newState);
                LOGGER.info(newState ? "Service is paused." : "Service has been resumed.");
                trayIcon.displayMessage("DownloadsWatchDog!",
                        newState ? "Service is paused." : "Service has been resumed.",
                        TrayIcon.MessageType.INFO);
                if(!isPaused.get()) {
                    System.out.println("--- 🧹 Starting Cleanup ---");
                    LOGGER.info("--- 🧹 Starting Cleanup ---");
                    for (Path folder : foldersToWatch) {
                        if (Files.exists(folder)) {
                            System.out.println("Processing: " + folder);
                            LOGGER.info("Processing: " + folder);
                            performInitialCleanup(folder);
                        } else {
                            System.out.println("⚠️ Warning: Folder not found: " + folder);
                            LOGGER.warning("⚠️ Warning: Folder not found: " + folder);
                        }
                    }
                    LOGGER.info("--- ✅ Cleanup Complete ---");
                }
            }
        };

        // Add a listener for double-clicking the icon
        trayIcon.addActionListener(new CleanupHandler());

        // 6. Add the icon to the SystemTray
        try {
            tray.add(trayIcon);
            LOGGER.info("Tray icon added.");
        } catch (AWTException e) {
            LOGGER.warning("TrayIcon could not be added.");
        }
    }

    // Helper method to generate a simple image just for this demo
    private static java.awt.Image GenerateImage() {
        int width = 16, height = 16;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw the white of the eye (an oval)
        g.setColor(Color.WHITE);
        g.fillOval(1, 3, 14, 10);

        // 2. Draw the outer outline (dark gray)
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new java.awt.BasicStroke(1.5f));
        g.drawOval(1, 3, 14, 10);

        // 3. Draw the iris/pupil (blue circle in the middle)
        g.setColor(new Color(0, 120, 215)); // A nice blue
        g.fillOval(5, 5, 6, 6);

        // 4. Optional: A tiny white reflection dot to make it look glossy
        g.setColor(Color.WHITE);
        g.fillOval(6, 6, 2, 2);

        g.dispose();
        return img;
    }
}