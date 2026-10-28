package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

/** Every command that takes a date accepts the same ways of writing it. */
public class DateFormatsInCommandsTest {

    private static final String[] OTHER_WAYS = {"22/12/2026", "22-12-26", "22 Dec 26", "22 Dec 2026", "2026/Dec/22"};

    private final AddressBookParser parser = new AddressBookParser(
            Clock.fixed(Instant.parse("2026-10-26T04:00:00Z"), ZoneId.of("Asia/Singapore")));

    private void assertSameAsIsoDate(String commandFormat) throws ParseException {
        Object iso = parser.parseCommand(String.format(commandFormat, "2026-12-22"));
        for (String date : OTHER_WAYS) {
            assertEquals(iso, parser.parseCommand(String.format(commandFormat, date)), date);
        }
    }

    @Test
    public void parseCommand_lessonDates_sameAsIsoDate() throws Exception {
        assertSameAsIsoDate("lesson add st/1 d/%s t/10:00");
        assertSameAsIsoDate("lesson move 1 d/%s");
        assertSameAsIsoDate("lesson repeat st/1 d/%s t/10:00 count/3");
        assertSameAsIsoDate("agenda d/%s");
    }

    @Test
    public void parseCommand_homeworkTaskAndPaymentDates_sameAsIsoDate() throws Exception {
        assertSameAsIsoDate("hw add st/1 desc/Algebra due/%s");
        assertSameAsIsoDate("hw edit 1 due/%s");
        assertSameAsIsoDate("task add st/1 desc/Call due/%s");
        assertSameAsIsoDate("task edit 1 desc/Call due/%s");
    }
}
