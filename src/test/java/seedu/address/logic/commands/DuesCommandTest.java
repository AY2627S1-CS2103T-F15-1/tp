package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;

public class DuesCommandTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-19T04:00:00Z"),
            ZoneId.of("Asia/Singapore"));

    @Test
    public void equalsAndToString_compareDateSourceAndRenderCommand() {
        DuesCommand command = new DuesCommand(CLOCK);
        DuesCommand equivalent = new DuesCommand(CLOCK);
        Clock anotherClock = Clock.fixed(Instant.parse("2026-10-20T04:00:00Z"), ZoneId.of("Asia/Singapore"));

        assertTrue(command.equals(command));
        assertTrue(command.equals(equivalent));
        assertFalse(command.equals(new DuesCommand(anotherClock)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new BalanceCommand(Index.fromOneBased(1))));
        assertTrue(command.toString().contains("DuesCommand"));
    }
}
