package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.UnsupportedSchemaException;
import seedu.address.model.ReadOnlyAddressBook;

/** Reads the current data file, with a separate legacy source for migration. */
public class VersionedAddressBookStorage extends JsonAddressBookStorage {
    private final Path legacy;
    private boolean isInitialSaveRequired;
    private boolean hasLegacyReadFailed;
    private Path readSource;

    /** Creates storage with separate current and legacy paths. */
    public VersionedAddressBookStorage(Path current, Path legacy) {
        super(current);
        this.legacy = legacy;
        readSource = current;
    }

    @Override
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        isInitialSaveRequired = false;
        hasLegacyReadFailed = false;
        readSource = getAddressBookFilePath();
        if (sourcePresence(getAddressBookFilePath()) != SourcePresence.ABSENT) {
            return super.readAddressBook();
        }
        readSource = legacy;
        try {
            Optional<ReadOnlyAddressBook> migrated = super.readAddressBook(legacy);
            isInitialSaveRequired = migrated.isPresent();
            return migrated;
        } catch (DataLoadingException e) {
            hasLegacyReadFailed = !(e.getCause() instanceof UnsupportedSchemaException);
            throw e;
        }
    }

    @Override
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        requireSupportedSource(readSource);
        if (sourcePresence(getAddressBookFilePath()) == SourcePresence.UNKNOWN) {
            throw new IOException("Restore access to the data files and restart before saving.");
        }
        if (hasLegacyReadFailed) {
            switch (sourcePresence(legacy)) {
                case PRESENT -> preserveUnreadableFile(legacy);
                case ABSENT -> {
                    // Startup may already have preserved the legacy source.
                }
                default -> throw new IOException("Restore access to the legacy source before saving.");
            }
            hasLegacyReadFailed = false;
        }
        super.saveAddressBook(addressBook);
    }

    public Path getReadSource() {
        return readSource;
    }

    public boolean requiresInitialSave() {
        return isInitialSaveRequired;
    }
}
