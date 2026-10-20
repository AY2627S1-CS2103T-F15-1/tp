package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ArchivedStudentStorageTest {

    @Test
    public void serialize_archivedStudent_writesTheArchivedKeyOfTheDataFile() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withArchived(true).build());

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));

        assertTrue(json.contains("\"archived\" : true"), json);
        assertFalse(json.contains("isArchived"), json);
    }

    @Test
    public void roundTrip_archivedStudent_staysArchivedWithTheSameIdentity() throws Exception {
        Person archived = new PersonBuilder().withArchived(true).build();
        AddressBook book = new AddressBook();
        book.addPerson(archived);

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        Person loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType()
                .getPersonList().get(0);

        assertTrue(loaded.isArchived());
        assertTrue(loaded.getId().equals(archived.getId()));
    }

    @Test
    public void roundTrip_activeStudent_staysActive() throws Exception {
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().build());

        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(book));
        Person loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType()
                .getPersonList().get(0);

        assertFalse(loaded.isArchived());
        assertTrue(json.contains("\"archived\" : false"), json);
    }
}
