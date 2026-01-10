import java.util.logging.Logger;

public class CategoryFinder {
    private static final Logger LOGGER = Logger.getLogger(CategoryFinder.class.getName());

    public CategoryFinder() {
        LOGGER.info("CategoryFinder initialized successfully.");
    }

    public String getCategory(String filename) {
        // 1. IMAGES
        if (isOneOf(filename, ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".svg", ".webp", ".ico", ".tiff")) {
            return returnFileType(filename, "Images");
        }

        // 2. VIDEO
        else if (isOneOf(filename, ".mp4", ".mkv", ".avi", ".mov", ".wmv", ".flv", ".webm", ".m4v")) {
            return returnFileType(filename, "Video");
        }

        // 3. AUDIO
        else if (isOneOf(filename, ".mp3", ".wav", ".flac", ".aac", ".ogg", ".wma", ".m4a")) {
            return returnFileType(filename, "Music");
        }

        // 4. DOCUMENTS (General)
        else if (isOneOf(filename, ".pdf", ".doc", ".docx", ".txt", ".rtf", ".odt", ".md", ".epub")) {
            return returnFileType(filename, "Documents");
        }

        // 5. SPREADSHEETS
        else if (isOneOf(filename, ".xls", ".xlsx", ".csv", ".ods", ".numbers")) {
            return returnFileType(filename, "Spreadsheets");
        }

        // 6. PRESENTATIONS
        else if (isOneOf(filename, ".ppt", ".pptx", ".odp", ".key")) {
            return returnFileType(filename, "Presentations");
        }

        // 7. ARCHIVES / COMPRESSED
        else if (isOneOf(filename, ".zip", ".rar", ".7z", ".tar", ".gz", ".iso", ".dmg")) {
            return returnFileType(filename, "Archives");
        }

        // 8. EXECUTABLES / INSTALLERS
        else if (isOneOf(filename, ".exe", ".msi", ".pkg", ".deb", ".rpm", ".bat", ".sh", ".apk")) {
            return returnFileType(filename, "Installers");
        }

        // 9. CODE / DEVELOPER FILES (Specific to you!)
        else if (isOneOf(filename, ".java", ".py", ".js", ".html", ".css", ".cpp", ".c", ".json", ".xml", ".sql")) {
            return returnFileType(filename, "Code");
        }

        return returnFileType(filename, "Other");
    }

    public String returnFileType(String fileName, String fileType) {
        LOGGER.info(fileName + " is of type " + fileType);
        return fileType;
    }

    private boolean isOneOf(String filename, String... extensions) {
        for (String ext : extensions) {
            if (filename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}