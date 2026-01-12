import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CategoryFinderTest {
    private CategoryFinder categoryFinder; // Declare it here

    @BeforeEach
        // Runs before every @Test
    void setUp() {
        categoryFinder = new CategoryFinder();
    }

    @Test
    void testDocuments() {
        // 1. Setup (Input)
        String input = "resume.pdf";

        // 2. Execution (Run the method)
        String result = categoryFinder.getCategory(input);

        // 3. Assertion (Check the result)
        // If result is NOT "Documents", the test fails and turns red.
        assertEquals("Documents", result);
    }

    @Test
    void testImages() {
        assertEquals("Images", categoryFinder.getCategory("photo.png"));
        assertEquals("Images", categoryFinder.getCategory("vacation.JPG")); // Case sensitivity check!
    }

    @Test
    void testUnknownFiles() {
        assertEquals("Other", categoryFinder.getCategory("weird_file.xyz"));
        assertEquals("Other", categoryFinder.getCategory("no_extension_file"));
    }

    @Test
    void testInstallers() {
        assertEquals("Installers", categoryFinder.getCategory("weird_file.exe"));
        assertEquals("Installers", categoryFinder.getCategory("extension_file.msi"));
    }
}
