package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    private final CommandHistory history = new CommandHistory();

    @Test
    public void previous_emptyHistory_returnsEmpty() {
        assertEquals(Optional.empty(), history.previous("draft"));
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void previous_steppingBack_walksFromNewestToOldestAndStops() {
        history.record("list");
        history.record("find amy");
        history.record("view 1");

        assertEquals(Optional.of("view 1"), history.previous(""));
        assertEquals(Optional.of("find amy"), history.previous("view 1"));
        assertEquals(Optional.of("list"), history.previous("find amy"));
        assertEquals(Optional.empty(), history.previous("list")); // the oldest command stays shown
        assertEquals(Optional.of("find amy"), history.next());
    }

    @Test
    public void next_pastNewest_returnsTheDraftTypedBeforeSteppingBack() {
        history.record("list");
        history.record("view 1");

        history.previous("find am");
        history.previous("view 1");

        assertEquals(Optional.of("view 1"), history.next());
        assertEquals(Optional.of("find am"), history.next());
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void previous_commandStillShownIsNewest_skipsIt() {
        history.record("list");
        history.record("bogus");

        assertEquals(Optional.of("list"), history.previous("bogus")); // "bogus" is what the box already shows
        assertEquals(Optional.of("bogus"), history.next()); // back to what was typed, shown once
        assertEquals(Optional.empty(), history.next());
    }

    @Test
    public void previous_onlyCommandIsStillShown_returnsEmpty() {
        history.record("bogus");

        assertEquals(Optional.empty(), history.previous("bogus"));
        assertEquals(Optional.empty(), history.next());
        assertEquals(Optional.of("bogus"), history.previous(""));
    }

    @Test
    public void record_resetsTheStepPosition() {
        history.record("list");
        history.record("view 1");
        history.previous("");
        history.previous("view 1");

        history.record("today");

        assertEquals(Optional.of("today"), history.previous(""));
    }

    @Test
    public void record_blankCommand_isNotKept() {
        history.record("list");
        history.record("");
        history.record("   ");

        assertEquals(Optional.of("list"), history.previous(""));
        assertEquals(Optional.empty(), history.previous("list"));
    }

    @Test
    public void record_sameCommandTwiceInARow_isKeptOnce() {
        history.record("list");
        history.record("list");

        assertEquals(Optional.of("list"), history.previous(""));
        assertEquals(Optional.empty(), history.previous("list"));
    }

    @Test
    public void record_sameCommandNotInARow_isKeptEachTime() {
        history.record("list");
        history.record("view 1");
        history.record("list");

        assertEquals(Optional.of("list"), history.previous(""));
        assertEquals(Optional.of("view 1"), history.previous("list"));
        assertEquals(Optional.of("list"), history.previous("view 1"));
    }

    @Test
    public void record_moreThanTheLimit_forgetsTheOldest() {
        for (int i = 0; i <= CommandHistory.MAX_ENTRIES; i++) {
            history.record("view " + (i + 1));
        }

        String oldest = "";
        for (Optional<String> shown = history.previous(""); shown.isPresent(); shown = history.previous(oldest)) {
            oldest = shown.get();
        }

        assertEquals("view 2", oldest); // "view 1" fell out of the history
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> history.record(null));
        assertThrows(NullPointerException.class, () -> history.previous(null));
    }
}
