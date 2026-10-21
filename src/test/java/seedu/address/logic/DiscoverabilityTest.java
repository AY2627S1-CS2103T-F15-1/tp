package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

/**
 * Checks that every command can be found: it is in the list of {@code help}, the parser knows it and the User Guide
 * describes it in a section of its own and in the command summary, so that no command can be added to one of them
 * and forgotten in the others.
 */
public class DiscoverabilityTest {
    private static final String SUMMARY_HEADING = "## Command summary";

    private static List<String> helpNames;
    private static String guide;

    @BeforeAll
    public static void readHelpAndGuide() throws Exception {
        String help = new HelpCommand().execute(new ModelManager()).getFeedbackToUser();
        helpNames = help.lines().filter(line -> line.startsWith("  "))
                .map(line -> line.substring(2).split("  ", 2)[0].trim()).toList();
        guide = readGuide();
    }

    private static String readGuide() throws IOException {
        return Files.readString(Path.of("docs", "UserGuide.md"));
    }

    private static String summary() {
        return guide.substring(guide.indexOf(SUMMARY_HEADING));
    }

    @Test
    public void help_listsEveryCommand() {
        assertTrue(helpNames.size() >= 33, "the command list was not read: " + helpNames);
        for (String name : new String[] {"archive", "restore", "student", "import students", "demo", "tour", "save",
            "hw add", "hw list", "hw edit", "hw status", "hw feedback", "hw delete", "lesson prep"}) {
            assertTrue(helpNames.contains(name), name + " is missing from help: " + helpNames);
        }
    }

    @Test
    public void parser_knowsEveryCommandThatHelpLists() {
        AddressBookParser parser = new AddressBookParser();

        for (String name : helpNames) {
            try {
                parser.parseCommand(name);
            } catch (ParseException e) {
                assertFalse(e.getMessage().equals(Messages.MESSAGE_UNKNOWN_COMMAND),
                        name + " is listed by help but the parser does not know it");
            }
        }
    }

    @Test
    public void userGuide_hasASectionForEveryCommandThatHelpLists() {
        for (String name : helpNames) {
            boolean hasSection = guide.lines().filter(line -> line.startsWith("#"))
                    .anyMatch(line -> line.contains("`" + name + "`"));
            assertTrue(hasSection, "the User Guide has no section headed with `" + name + "`");
        }
    }

    @Test
    public void userGuide_commandSummaryHasARowForEveryCommandThatHelpLists() {
        String summary = summary();

        for (String name : helpNames) {
            assertTrue(summary.contains("`" + name + " ") || summary.contains("`" + name + "`"),
                    "the command summary has no row for " + name);
        }
        long rows = summary.lines().filter(line -> line.startsWith("**")).count();
        assertEquals(helpNames.size(), rows, "the command summary and help list a different number of commands");
    }

    @Test
    public void userGuide_commandSummaryHasNoRowForACommandThatHelpDoesNotList() {
        String summary = summary();
        List<String> rowFormats = summary.lines().filter(line -> line.startsWith("**"))
                .map(line -> line.split("\\|", 2)[1].trim()).collect(Collectors.toList());

        for (String format : rowFormats) {
            boolean isListed = helpNames.stream()
                    .anyMatch(name -> format.startsWith("`" + name + " ") || format.startsWith("`" + name + "`"));
            assertTrue(isListed, "the summary lists a command that help does not: " + format);
        }
    }
}
