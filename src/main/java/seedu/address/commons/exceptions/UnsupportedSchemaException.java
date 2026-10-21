package seedu.address.commons.exceptions;

/** Identifies a data version that must remain in place for a compatible application. */
public class UnsupportedSchemaException extends IllegalValueException {
    /** Explains which data versions can be opened without risking loss of newer fields. */
    public UnsupportedSchemaException(String version, int minimum, int current) {
        super("Unsupported data schema version: " + version + ". This build supports versions "
                + minimum + " through " + current + ". Open with a compatible TutorFlow build or restore "
                + "a matching backup. The original data file has not been changed.");
    }
}
