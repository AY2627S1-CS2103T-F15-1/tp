package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

/**
 * The files that TutorFlow keeps for itself, relative to the folder from which it is started: the data file, the
 * older data file that it migrates, and the preferences, together with the recovery and temporary copies that it
 * makes beside them.
 */
public final class DataFiles {

    public static final Path DATA_FILE = Paths.get("data", "tutorflow-v14.json");
    public static final Path LEGACY_FILE = Paths.get("data", "addressbook.json");
    public static final Path PREFERENCES_FILE = Paths.get("preferences.json");

    private static final String TEMPORARY_PREFIX = ".tutorflow-";
    private static final String TEMPORARY_SUFFIX = ".tmp";

    private DataFiles() {
    }

    /**
     * Returns whether writing to {@code candidate} would damage a file that TutorFlow uses, however the path is
     * spelled: with folders such as {@code ./} or {@code ../}, as an absolute path, in other letter case, or through
     * a link to the file.
     */
    public static boolean isUsedByTutorFlow(Path candidate) {
        return isUsedBy(candidate, List.of(DATA_FILE, LEGACY_FILE, PREFERENCES_FILE));
    }

    /** Returns whether writing to {@code candidate} would damage one of {@code ownFiles} or a copy beside it. */
    static boolean isUsedBy(Path candidate, List<Path> ownFiles) {
        requireNonNull(candidate);
        Path target = canonical(candidate);
        for (Path own : ownFiles) {
            Path ownFile = canonical(own);
            if (hasSameFolder(target, ownFile) && (hasSameName(target, ownFile) || isCopyOf(target, ownFile))) {
                return true;
            }
            if (isSameFile(target, ownFile)) {
                return true;
            }
        }
        return isTemporaryCopy(target) && ownFiles.stream().anyMatch(own -> hasSameFolder(target, canonical(own)));
    }

    /** Returns the absolute path of {@code path}, with its folders resolved through any links. */
    private static Path canonical(Path path) {
        Path absolute = path.toAbsolutePath().normalize();
        Path folder = absolute.getParent();
        if (folder == null) {
            return absolute;
        }
        try {
            if (Files.exists(folder)) {
                folder = folder.toRealPath();
            }
        } catch (IOException e) {
            // an unreadable folder is compared by its written path
        }
        return folder.resolve(absolute.getFileName());
    }

    private static boolean hasSameFolder(Path first, Path second) {
        return first.getParent() != null && first.getParent().toString().equalsIgnoreCase(
                String.valueOf(second.getParent()));
    }

    private static boolean hasSameName(Path first, Path second) {
        return first.getFileName().toString().equalsIgnoreCase(second.getFileName().toString());
    }

    /** Returns whether {@code target} is a recovery copy of {@code own}, such as {@code own.corrupt.1}. */
    private static boolean isCopyOf(Path target, Path own) {
        return target.getFileName().toString().toLowerCase(Locale.ROOT)
                .startsWith(own.getFileName().toString().toLowerCase(Locale.ROOT) + ".");
    }

    private static boolean isTemporaryCopy(Path target) {
        String name = target.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.startsWith(TEMPORARY_PREFIX) && name.endsWith(TEMPORARY_SUFFIX);
    }

    private static boolean isSameFile(Path first, Path second) {
        try {
            return Files.exists(first) && Files.exists(second) && Files.isSameFile(first, second);
        } catch (IOException e) {
            return false;
        }
    }
}
