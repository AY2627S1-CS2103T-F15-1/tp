package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.commons.exceptions.UnsupportedSchemaException;

public class StorageSchemaTest {
    private static final int OLDEST = StorageSchema.MINIMUM_VERSION;
    private static final int NEWEST = StorageSchema.CURRENT_VERSION;

    private static String refusal(String version) {
        return "Unsupported data schema version: " + version + ". This build supports versions " + OLDEST
                + " through " + NEWEST + ". Open with a compatible TutorFlow build or restore a matching backup. "
                + "The original data file has not been changed.";
    }

    private static JsonNode document(String json) throws Exception {
        return new ObjectMapper().readTree(json);
    }

    @Test
    public void supportedRange_startsAtFourteenAndEndsAtTheVersionThatIsWritten() {
        assertEquals(14, OLDEST);
        assertTrue(NEWEST >= OLDEST, "the version that is written is supported");
    }

    @Test
    public void requireSupported_unversionedOrEverySupportedVersion_accepted() {
        assertDoesNotThrow(() -> StorageSchema.requireSupported(null));
        for (int version = OLDEST; version <= NEWEST; version++) {
            int supported = version;
            assertDoesNotThrow(() -> StorageSchema.requireSupported(supported));
        }
    }

    @Test
    public void requireSupported_anyOtherVersion_refusedWithTheSpecifiedMessage() {
        for (int version : new int[] {Integer.MIN_VALUE, -1, 0, 1, OLDEST - 1, NEWEST + 1, 99, Integer.MAX_VALUE}) {
            Executable load = () -> StorageSchema.requireSupported(version);

            assertEquals(refusal(String.valueOf(version)),
                    assertThrows(UnsupportedSchemaException.class, load).getMessage());
        }
    }

    @Test
    public void requireSupportedDocument_noVersionField_accepted() throws Exception {
        assertDoesNotThrow(() -> StorageSchema.requireSupportedDocument(document("{\"persons\":[]}")));
        assertDoesNotThrow(() -> StorageSchema.requireSupportedDocument(document("{\"schemaVersion\":null}")));
    }

    @Test
    public void requireSupportedDocument_supportedVersions_accepted() throws Exception {
        for (int version = OLDEST; version <= NEWEST; version++) {
            JsonNode supported = document("{\"schemaVersion\":" + version + "}");
            assertDoesNotThrow(() -> StorageSchema.requireSupportedDocument(supported));
        }
    }

    @Test
    public void requireSupportedDocument_unsupportedOrMalformedVersion_refused() throws Exception {
        for (String version : new String[] {String.valueOf(OLDEST - 1), String.valueOf(NEWEST + 1),
            "\"" + NEWEST + "\"", NEWEST + ".5", "true", "[" + NEWEST + "]"}) {
            JsonNode refused = document("{\"schemaVersion\":" + version + "}");

            Executable load = () -> StorageSchema.requireSupportedDocument(refused);

            assertEquals(refusal(version), assertThrows(UnsupportedSchemaException.class, load).getMessage());
        }
    }
}
