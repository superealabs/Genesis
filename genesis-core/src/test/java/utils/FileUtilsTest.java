package utils;

import org.junit.jupiter.api.Test;
import org.labs.utils.FileUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileUtilsTest {

    @Test
    public void testGetFileContent_ExistingFile() throws IOException {
        // Test reading an existing file
        String content = FileUtils.getFileContent("test_file.txt");
        String expected = "This is a test file.\nIt has multiple lines.\nUsed for testing the FileUtils.getFileContent method.\n";
        assertEquals(expected, content);
    }

    @Test
    public void testGetFileContent_NonExistentFile() {
        // Test reading a non-existent file
        assertThrows(IOException.class, () -> {
            FileUtils.getFileContent("non_existent_file.txt");
        });
    }
}