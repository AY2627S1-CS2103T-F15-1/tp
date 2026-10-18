package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyAddressBook;

/** Reads the current data file, with a separate legacy source for migration. */
public class VersionedAddressBookStorage extends JsonAddressBookStorage {
    private final Path legacy;
    private boolean initialSave;
    private boolean inaccessibleLegacy;

    /** Creates storage with separate current and legacy paths. */
    public VersionedAddressBookStorage(Path current, Path legacy) {
        super(current);
        this.legacy = legacy;
    }

    @Override
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        initialSave = false;
        inaccessibleLegacy = false;
        if (sourcePresence(getAddressBookFilePath()) != SourcePresence.ABSENT) {
            return super.readAddressBook();
        }
        if (sourcePresence(legacy) == SourcePresence.UNKNOWN) {
            inaccessibleLegacy = true;
            throw new DataLoadingException(new IOException("Cannot inspect the legacy data file."));
        }
        Optional<ReadOnlyAddressBook> migrated = super.readAddressBook(legacy);
        initialSave = migrated.isPresent();
        return migrated;
    }

    @Override
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        if (sourcePresence(getAddressBookFilePath()) == SourcePresence.UNKNOWN || inaccessibleLegacy) {
            throw new IOException("Restore access to the data files and restart before saving.");
        }
        super.saveAddressBook(addressBook);
    }

    public boolean requiresInitialSave() {
        return initialSave;
    }
}
