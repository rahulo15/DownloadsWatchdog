import java.nio.file.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        List<Path> foldersToWatch = new ArrayList<>();

        foldersToWatch.add(Paths.get(System.getProperty("user.home"), "Downloads"));
        foldersToWatch.add(Paths.get(System.getProperty("user.home"), "Desktop"));
        foldersToWatch.add(Paths.get("U:"));
        foldersToWatch.add(Paths.get("O:"));

        WatchService watchdog = FileSystems.getDefault().newWatchService();
        System.out.println("--- 🧹 Starting Initial Cleanup & Registration ---");
        for (Path folder : foldersToWatch) {
            if (Files.exists(folder)) {
                System.out.println("Processing: " + folder);
                performInitialCleanup(folder);
                folder.register(watchdog, StandardWatchEventKinds.ENTRY_CREATE);
            } else {
                System.out.println("⚠️ Warning: Folder not found: " + folder);
            }
        }
        System.out.println("--- ✅ Cleanup Complete ---");
        System.out.println("--- 👀 Watchdog is watching " + foldersToWatch.size() + " folders ---");
        startWatchDog(watchdog);
    }

    private static void performInitialCleanup(Path folder) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
            for (Path file : stream) {
                processFile(file, folder, false);
            }
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + folder + ": " + e.getMessage());
        }
    }

    private static void startWatchDog(WatchService watchdog) throws IOException, InterruptedException {
        //runs indefinitely
        while (true) {
            WatchKey key = watchdog.take();
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
            System.out.println("✅ Moved " + file.getFileName() + " -> " + targetFolderName);
        } catch (Exception e) {
            System.out.println("❌ Failed to move file: " + file.getFileName());
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
}