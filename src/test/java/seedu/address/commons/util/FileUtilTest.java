package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that failed writes preserve the previously saved data.
 */
public class FileUtilTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void writeToFile_interruptedWrite_preservesOriginalAndRemovesTemporaryFile() throws Exception {
        Path file = temporaryFolder.resolve("data.json");
        Files.writeString(file, "previous data");
        InputStream failingContent = new InputStream() {
            private boolean hasReturnedByte;

            @Override
            public int read() throws IOException {
                if (hasReturnedByte) {
                    throw new IOException("Simulated interrupted write");
                }
                hasReturnedByte = true;
                return 'x';
            }
        };

        assertThrows(IOException.class, () -> FileUtil.writeToFile(file, failingContent));

        assertEquals("previous data", Files.readString(file));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    public void writeToFile_existingFile_replacesWholeContentAndRemovesTemporaryFile() throws Exception {
        Path file = temporaryFolder.resolve("data.json");
        Files.writeString(file, "previous longer data");

        FileUtil.writeToFile(file, "\u9648\u4f1f\u660e");

        assertEquals("\u9648\u4f1f\u660e", Files.readString(file));
        try (var files = Files.list(temporaryFolder)) {
            assertEquals(1, files.count());
        }
    }
}
