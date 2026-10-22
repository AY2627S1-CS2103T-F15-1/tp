package seedu.address.storage;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.UnsupportedSchemaException;

/** Defines the supported version range for the shared TutorFlow data file. */
public final class StorageSchema {
    public static final int MINIMUM_VERSION = 14;
    public static final int CURRENT_VERSION = 16;

    private StorageSchema() {
    }

    /** Accepts unversioned legacy data and every known schema in the supported range. */
    public static void requireSupported(Integer version) throws UnsupportedSchemaException {
        if (version != null && (version < MINIMUM_VERSION || version > CURRENT_VERSION)) {
            throw new UnsupportedSchemaException(version.toString(), MINIMUM_VERSION, CURRENT_VERSION);
        }
    }

    /** Checks the version before binding fields whose representation may differ in a later schema. */
    public static void requireSupportedDocument(JsonNode document) throws UnsupportedSchemaException {
        JsonNode version = document.get("schemaVersion");
        if (version == null || version.isNull()) {
            return;
        }
        if (!version.isInt()) {
            throw new UnsupportedSchemaException(version.toString(), MINIMUM_VERSION, CURRENT_VERSION);
        }
        requireSupported(version.intValue());
    }
}
