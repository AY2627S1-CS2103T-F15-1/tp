package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.exceptions.UnsupportedSchemaException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;
    private final Set<Path> unreadableFiles = new HashSet<>();
    private final Set<Path> unsupportedFiles = new HashSet<>();
    private final Set<Path> inaccessibleFiles = new HashSet<>();

    /** Separates positively absent files from sources whose metadata cannot be read. */
    protected enum SourcePresence { PRESENT, ABSENT, UNKNOWN }

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    /** Preserves the unreadable source before the next replacement. */
    protected void preserveUnreadableFile(Path path) throws IOException {
        DataFileRecovery.preserve(path);
    }

    /** Queries metadata without treating an inconclusive existence check as evidence of absence. */
    protected SourcePresence sourcePresence(Path path) {
        return Files.exists(path) ? SourcePresence.PRESENT
                : Files.notExists(path) ? SourcePresence.ABSENT : SourcePresence.UNKNOWN;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        try {
            return readModel(filePath);
        } catch (DataLoadingException e) {
            Path normalizedPath = filePath.toAbsolutePath().normalize();
            if (e.getCause() instanceof UnsupportedSchemaException) {
                unsupportedFiles.add(normalizedPath);
            } else if (isAccessFailure(e)) {
                inaccessibleFiles.add(normalizedPath);
            } else {
                unreadableFiles.add(normalizedPath);
            }
            throw e;
        }
    }

    /**
     * Returns whether {@code e} says that the file could not be opened or read, as when another program holds it or
     * its permissions forbid it, and not that its content is invalid. A file that cannot be opened may be valid, so
     * it must never be treated as corrupt, which would set it aside.
     */
    public static boolean isAccessFailure(DataLoadingException e) {
        return e.getCause() instanceof IOException && !(e.getCause() instanceof JsonProcessingException);
    }

    /** Reads and validates data without changing the protected-source state. */
    private Optional<ReadOnlyAddressBook> readModel(Path filePath) throws DataLoadingException {
        if (sourcePresence(filePath) == SourcePresence.UNKNOWN) {
            throw new DataLoadingException(new IOException("Cannot determine whether the data file exists."));
        }
        Optional<JsonNode> jsonAddressBook = JsonUtil.readJsonFile(filePath, JsonNode.class);
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            if (!jsonAddressBook.get().isObject()) {
                throw new IllegalValueException("JSON data must be an object.");
            }
            StorageSchema.requireSupportedDocument(jsonAddressBook.get());
            return Optional.of(JsonUtil.fromJsonTree(jsonAddressBook.get(), JsonSerializableAddressBook.class)
                    .toModelType());
        } catch (IllegalValueException | IOException ive) {
            logger.warning("Data file rejected: invalid field values");
            throw new DataLoadingException(ive);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        requireSupportedSource(filePath);
        requireOpenedSource(filePath);
        Path normalizedPath = filePath.toAbsolutePath().normalize();
        if (unreadableFiles.contains(normalizedPath)) {
            switch (sourcePresence(filePath)) {
                case PRESENT -> preserveUnreadableFile(filePath);
                case ABSENT -> {
                    // Startup may already have moved the protected original to its recovery copy.
                }
                default -> throw new IOException("Cannot determine whether the unreadable source exists. "
                        + "Restore access and retry saving.");
            }
            unreadableFiles.remove(normalizedPath);
        }
        writeSnapshot(filePath, snapshotAddressBook(addressBook));
    }

    /** A rejected schema must never enter the corruption recovery or overwrite paths. */
    protected void requireSupportedSource(Path source) throws IOException {
        if (unsupportedFiles.contains(source.toAbsolutePath().normalize())) {
            throw new IOException("Unsupported data schema. Open with a compatible TutorFlow build "
                    + "or restore a matching backup and restart before saving.");
        }
    }

    /** A file that could not be opened at start-up must not be replaced, because it may hold valid records. */
    protected void requireOpenedSource(Path source) throws IOException {
        if (inaccessibleFiles.contains(source.toAbsolutePath().normalize())) {
            throw new IOException(String.format(DataFileRecovery.MESSAGE_NOT_OPENED_SAVE, source));
        }
    }

    /** Writes a serialized snapshot through the atomic file writer. */
    protected void writeSnapshot(Path filePath, String snapshot) throws IOException {
        FileUtil.writeToFile(filePath, snapshot);
    }

    /** Uses the same representation as saving, so future persisted fields also participate in change detection. */
    public String snapshotAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        return JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook));
    }
}
