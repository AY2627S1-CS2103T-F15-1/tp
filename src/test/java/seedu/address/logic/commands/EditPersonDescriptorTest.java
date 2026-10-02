package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_LEVEL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_RATE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_SUBJECT_PHYSICS;
import static seedu.address.logic.commands.CommandTestUtil.VALID_VENUE_BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditPersonDescriptorTest {

    @Test
    public void equals() {
        // same values -> returns true
        EditPersonDescriptor descriptorWithSameValues = new EditPersonDescriptor(DESC_AMY);
        assertTrue(DESC_AMY.equals(descriptorWithSameValues));

        // same object -> returns true
        assertTrue(DESC_AMY.equals(DESC_AMY));

        // null -> returns false
        assertFalse(DESC_AMY.equals(null));

        // different types -> returns false
        assertFalse(DESC_AMY.equals(5));

        // different values -> returns false
        assertFalse(DESC_AMY.equals(DESC_BOB));

        // different name -> returns false
        EditPersonDescriptor editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withName(VALID_NAME_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different phone -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withPhone(VALID_PHONE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different email -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different level -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withLevel(VALID_LEVEL_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different subjects -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withSubjects(VALID_SUBJECT_PHYSICS).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different rate -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withRate(VALID_RATE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));

        // different venue -> returns false
        editedAmy = new EditPersonDescriptorBuilder(DESC_AMY).withVenue(VALID_VENUE_BOB).build();
        assertFalse(DESC_AMY.equals(editedAmy));
    }

    @Test
    public void toStringMethod() {
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        String expected = EditPersonDescriptor.class.getCanonicalName() + "{name="
                + editPersonDescriptor.getName().orElse(null) + ", phone="
                + editPersonDescriptor.getPhone().orElse(null) + ", email="
                + editPersonDescriptor.getEmail().orElse(null) + ", level="
                + editPersonDescriptor.getLevel().orElse(null) + ", subjects="
                + editPersonDescriptor.getSubjects().orElse(null) + ", rate="
                + editPersonDescriptor.getRate().orElse(null) + ", venue="
                + editPersonDescriptor.getVenue().orElse(null) + "}";
        assertEquals(expected, editPersonDescriptor.toString());
    }
}
