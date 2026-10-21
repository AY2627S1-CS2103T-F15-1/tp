package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ImportStudentsCommand;

public class ImportCommandParserTest {
    private static final String INVALID_FORMAT = "Invalid command format!\n" + ImportStudentsCommand.MESSAGE_USAGE;

    private final ImportCommandParser parser = new ImportCommandParser();

    private static ImportStudentsCommand command(String path, boolean isConfirmed) {
        return new ImportStudentsCommand(Path.of(path), path, isConfirmed);
    }

    @Test
    public void parse_validArguments_success() {
        assertParseSuccess(parser, " students file/students.csv", command("students.csv", false));
        assertParseSuccess(parser, " students file/students.csv confirm", command("students.csv", true));
        assertParseSuccess(parser, "  students   file/data/old students.csv  confirm ",
                command("data/old students.csv", true));
        assertParseSuccess(parser, " students file/\"students confirm\"", command("students confirm", false));
        assertParseSuccess(parser, " students file/\"My Documents/old students.csv\" confirm",
                command("My Documents/old students.csv", true));
    }

    @Test
    public void parse_missingParts_invalidCommandFormat() {
        assertParseFailure(parser, "", INVALID_FORMAT);
        assertParseFailure(parser, " students", INVALID_FORMAT);
        assertParseFailure(parser, " students confirm", INVALID_FORMAT);
        assertParseFailure(parser, " students students.csv", INVALID_FORMAT);
        assertParseFailure(parser, " file/students.csv", INVALID_FORMAT);
        assertParseFailure(parser, " payments file/students.csv", INVALID_FORMAT);
        assertParseFailure(parser, " Students file/students.csv", INVALID_FORMAT);
    }

    @Test
    public void parse_textBeforeThePrefix_invalidCommandFormat() {
        assertParseFailure(parser, " students now file/students.csv", INVALID_FORMAT);
        assertParseFailure(parser, " students confirm file/students.csv", INVALID_FORMAT);
    }

    @Test
    public void parse_emptyOrBadPath_namesTheProblem() {
        assertParseFailure(parser, " students file/", PathArgument.MESSAGE_EMPTY_PATH);
        assertParseFailure(parser, " students file/ confirm", PathArgument.MESSAGE_EMPTY_PATH);
        assertParseFailure(parser, " students file/\"a.csv", PathArgument.MESSAGE_UNCLOSED_QUOTE);
        assertParseFailure(parser, " students file/\"a.csv\" soon", INVALID_FORMAT);
        assertParseFailure(parser, " students file/a\0b.csv",
                String.format(ImportCommandParser.MESSAGE_INVALID_PATH, "a\0b.csv"));
    }
}
