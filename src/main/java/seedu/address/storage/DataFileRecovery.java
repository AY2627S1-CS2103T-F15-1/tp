package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Keeps a data file that could not be read, so that starting afresh never destroys the only copy of a tutor's
 * records.
 */
public final class DataFileRecovery {

    public static final String CORRUPT_SUFFIX = ".corrupt";

    public static final String MESSAGE_SET_ASIDE = "Your data file at %1$s could not be read and has been set "
            + "aside as %2$s. TutorFlow has started with an empty list. The original is never overwritten in place.";
    public static final String MESSAGE_NOT_SET_ASIDE = "Your data file at %1$s could not be read, and it could "
            + "not be set aside (%2$s). TutorFlow has started with an empty list. Copy that file somewhere safe "
            + "for recovery. Saving will retry preservation and is blocked until the original can be preserved.";

    private static final Logger logger = LogsCenter.getLogger(DataFileRecovery.class);

    private DataFileRecovery() {} // prevents instantiation

    /**
     * Renames {@code dataFile} so that it is no longer the data file, and returns a warning for the user that says
     * where it went. An earlier set-aside file is never replaced: a number is added to the name instead.
     * If the file cannot be renamed, the warning says so.
     */
    public static String setAside(Path dataFile) {
        try {
            Path setAsideFile = preserve(dataFile);
            logger.warning("Data file " + dataFile + " could not be read and was set aside as " + setAsideFile);
            return String.format(MESSAGE_SET_ASIDE, dataFile, setAsideFile);
        } catch (IOException e) {
            logger.warning("Data file " + dataFile + " could not be read or set aside: " + e);
            return String.format(MESSAGE_NOT_SET_ASIDE, dataFile, e.getMessage());
        }
    }

    /** Moves an unreadable source without replacing an earlier backup; failure prevents the caller's save. */
    public static Path preserve(Path dataFile) throws IOException {
        Path target = dataFile.resolveSibling(dataFile.getFileName() + CORRUPT_SUFFIX);
        for (int copy = 1; Files.exists(target); copy++) {
            target = dataFile.resolveSibling(dataFile.getFileName() + CORRUPT_SUFFIX + "." + copy);
        }
        return Files.move(dataFile, target);
    }
}
