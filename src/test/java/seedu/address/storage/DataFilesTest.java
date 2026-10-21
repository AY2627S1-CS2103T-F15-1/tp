package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DataFilesTest {

    private static void assertUsed(String path) {
        assertTrue(DataFiles.isUsedByTutorFlow(Paths.get(path)), path);
    }

    private static void assertNotUsed(String path) {
        assertFalse(DataFiles.isUsedByTutorFlow(Paths.get(path)), path);
    }

    @Test
    public void isUsedByTutorFlow_itsOwnFiles_whateverTheSpelling() {
        assertUsed("data/tutorflow-v14.json");
        assertUsed("./data/tutorflow-v14.json");
        assertUsed("data/../data/tutorflow-v14.json");
        assertUsed("data//tutorflow-v14.json");
        assertUsed("DATA/TutorFlow-V14.JSON");
        assertUsed("data/addressbook.json");
        assertUsed("preferences.json");
        assertUsed("./Preferences.JSON");
        assertUsed("somewhere/../preferences.json");
        assertUsed(Paths.get("data", "tutorflow-v14.json").toAbsolutePath().toString());
        assertUsed(Paths.get("preferences.json").toAbsolutePath().toString());
    }

    @Test
    public void isUsedByTutorFlow_recoveryAndTemporaryCopies() {
        assertUsed("data/tutorflow-v14.json.corrupt");
        assertUsed("data/tutorflow-v14.json.corrupt.3");
        assertUsed("data/addressbook.json.corrupt");
        assertUsed("preferences.json.corrupt");
        assertUsed("data/.tutorflow-12345.tmp");
        assertUsed(".tutorflow-12345.tmp");
    }

    @Test
    public void isUsedByTutorFlow_otherFiles_areFree() {
        assertNotUsed("payments.csv");
        assertNotUsed("data/payments.csv");
        assertNotUsed("data/tutorflow-v14.json5");
        assertNotUsed("data/tutorflow-v14.csv");
        assertNotUsed("backup/tutorflow-v14.json");
        assertNotUsed("backup/preferences.json");
        assertNotUsed("preferences.csv");
        assertNotUsed("other/.tutorflow-12345.tmp");
        assertNotUsed("data/old/addressbook.json");
    }

    @Test
    public void isUsedBy_aLinkToAFileOfTutorFlow_isRefusedWhateverItIsCalled() throws Exception {
        Path folder = Files.createTempDirectory("tutorflow-links");
        Path data = Files.createDirectories(folder.resolve("data")).resolve("tutorflow-v14.json");
        Files.writeString(data, "{}");
        Path link = folder.resolve("export.csv");
        Path hardLink = folder.resolve("copy.csv");
        try {
            Files.createSymbolicLink(link, data);
            Files.createLink(hardLink, data);
            List<Path> own = List.of(data);

            assertTrue(DataFiles.isUsedBy(link, own));
            assertTrue(DataFiles.isUsedBy(hardLink, own));
            assertTrue(DataFiles.isUsedBy(folder.resolve("data").resolve("..").resolve("data")
                    .resolve("TUTORFLOW-V14.json"), own));
            assertFalse(DataFiles.isUsedBy(folder.resolve("other.csv"), own));
        } catch (UnsupportedOperationException | IOException e) {
            // links are not available on this platform, so the names are all that can be compared
        } finally {
            Files.deleteIfExists(link);
            Files.deleteIfExists(hardLink);
            Files.deleteIfExists(data);
            Files.deleteIfExists(data.getParent());
            Files.deleteIfExists(folder);
        }
    }

    @Test
    public void isUsedBy_ownFilesInAnotherFolder_areComparedByTheirRealPlace() throws Exception {
        Path folder = Files.createTempDirectory("tutorflow-own");
        Path data = Files.createDirectories(folder.resolve("data")).resolve("tutorflow-v14.json");
        try {
            List<Path> own = List.of(data);

            assertTrue(DataFiles.isUsedBy(data, own));
            assertTrue(DataFiles.isUsedBy(data.resolveSibling(".tutorflow-77.tmp"), own));
            assertTrue(DataFiles.isUsedBy(data.resolveSibling("tutorflow-v14.json.corrupt"), own));
            assertFalse(DataFiles.isUsedBy(folder.resolve("tutorflow-v14.json"), own));
        } finally {
            Files.deleteIfExists(data.getParent());
            Files.deleteIfExists(folder);
        }
    }

    @Test
    public void isUsedByTutorFlow_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DataFiles.isUsedByTutorFlow(null));
    }
}
