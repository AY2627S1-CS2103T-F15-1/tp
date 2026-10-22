package seedu.address.model.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class HomeworkStatusTest {

    @Test
    public void fromText_everyStatusInAnyLetterCase_isRecognized() {
        for (HomeworkStatus status : HomeworkStatus.values()) {
            String name = status.toString();
            assertEquals(Optional.of(status), HomeworkStatus.fromText(name));
            assertEquals(Optional.of(status), HomeworkStatus.fromText(name.toUpperCase()));
            assertEquals(Optional.of(status), HomeworkStatus.fromText(" " + name.substring(0, 1).toUpperCase()
                    + name.substring(1) + "  "));
        }
    }

    @Test
    public void fromText_derivedOrUnknownWords_areNotStatuses() {
        for (String text : new String[] {"overdue", "OVERDUE", "done", "submit", "", "  ", "assigned submitted"}) {
            assertTrue(HomeworkStatus.fromText(text).isEmpty(), text);
        }
    }

    @Test
    public void toString_isLowerCaseAsShownToTheUser() {
        assertEquals("assigned", HomeworkStatus.ASSIGNED.toString());
        assertEquals("submitted", HomeworkStatus.SUBMITTED.toString());
        assertEquals("reviewed", HomeworkStatus.REVIEWED.toString());
    }

    @Test
    public void constraintsMessage_matchesTheSpecification() {
        assertEquals("Status must be one of: assigned, submitted, reviewed.", HomeworkStatus.MESSAGE_CONSTRAINTS);
    }
}
