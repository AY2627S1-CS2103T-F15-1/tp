package seedu.address.logic.parser;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.ImportStudentsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code import students file/PATH [confirm]}. */
public class ImportCommandParser implements Parser<Command> {

    public static final String MESSAGE_INVALID_PATH = "That is not a valid file path: %1$s";

    private static final String SUBCOMMAND = "students";
    private static final String FILE_PREFIX = "file/";

    @Override
    public Command parse(String args) throws ParseException {
        String[] parts = args.strip().split("\\s+", 2);
        if (!parts[0].equals(SUBCOMMAND) || parts.length < 2 || !parts[1].startsWith(FILE_PREFIX)) {
            throw ParserUtil.invalidFormat(ImportStudentsCommand.MESSAGE_USAGE);
        }
        PathArgument argument = PathArgument.parse(parts[1].substring(FILE_PREFIX.length()),
                ImportStudentsCommand.MESSAGE_USAGE);
        try {
            return new ImportStudentsCommand(Path.of(argument.getPath()), argument.getPath(),
                    argument.isConfirmed());
        } catch (InvalidPathException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_PATH, argument.getPath()), e);
        }
    }
}
