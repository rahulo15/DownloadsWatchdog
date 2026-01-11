import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

public class CategoryFinder {
    private static final Logger LOGGER = Logger.getLogger(CategoryFinder.class.getName());

    public CategoryFinder() {
        LOGGER.info("CategoryFinder initialized successfully.");
    }

    public String getCategory(String filename) {
        String ext = FilenameUtils.getExtension(filename).toLowerCase();
        return switch (ext) {
            // 1. IMAGES
            case "jpg", "jpeg", "png", "gif", "bmp", "svg", "webp", "ico", "tiff" -> returnFileType(filename, "Images");

            // 2. VIDEO
            case "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v" -> returnFileType(filename, "Video");

            // 3. AUDIO
            case "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a" -> returnFileType(filename, "Music");

            // 4. DOCUMENTS
            case "pdf", "doc", "docx", "txt", "rtf", "odt", "md", "epub" -> returnFileType(filename, "Documents");

            // 5. SPREADSHEETS
            case "xls", "xlsx", "csv", "ods", "numbers" -> returnFileType(filename, "Spreadsheets");

            // 6. PRESENTATIONS
            case "ppt", "pptx", "odp", "key" -> returnFileType(filename, "Presentations");

            // 7. ARCHIVES
            case "zip", "rar", "7z", "tar", "gz", "iso", "dmg" -> returnFileType(filename, "Archives");

            // 8. INSTALLERS
            case "exe", "msi", "pkg", "deb", "rpm", "bat", "sh", "apk" -> returnFileType(filename, "Installers");

            // 9. CODE
            case "java", "py", "js", "html", "css", "cpp", "c", "json", "xml", "sql" -> returnFileType(filename, "Code");

            //10. DEFAULT
            default -> returnFileType(filename, "Other");
        };
    }

    public String returnFileType(String fileName, String fileType) {
        LOGGER.info(fileName + " is of type " + fileType);
        return fileType;
    }
}