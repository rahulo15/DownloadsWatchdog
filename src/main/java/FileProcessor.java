import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class FileProcessor {
    private static final Logger LOGGER = Logger.getLogger(FileProcessor.class.getName());
    private final CategoryFinder categoryFinder;

    public FileProcessor() {
        this.categoryFinder = new CategoryFinder();
    }

    public void processFile(Path file, Path parentFolder, boolean requiresDelay) {
        String fileNameStr = file.getFileName().toString().toLowerCase();
        if (Files.isDirectory(file)) return;
        String targetFolderName = this.categoryFinder.getCategory(fileNameStr);
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
}
