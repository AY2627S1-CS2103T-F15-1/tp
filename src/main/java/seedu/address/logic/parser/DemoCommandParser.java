package seedu.address.logic.parser;

import seedu.address.logic.commands.DemoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code demo}, {@code demo confirm} and {@code demo exit}. */
public class DemoCommandParser implements Parser<DemoCommand> {

    @Override
    public DemoCommand parse(String args) throws ParseException {
        return switch (args.strip()) {
            case "" -> new DemoCommand(DemoCommand.Action.PREVIEW);
            case DemoCommand.CONFIRM -> new DemoCommand(DemoCommand.Action.START);
            case DemoCommand.EXIT -> new DemoCommand(DemoCommand.Action.EXIT);
            default -> throw ParserUtil.invalidFormat(DemoCommand.MESSAGE_USAGE);
        };
    }
}
