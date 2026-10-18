package seedu.address.logic.parser;

import java.math.BigDecimal;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.finance.Money;

/** Parses exact positive money without accepting exponents, separators, or silent rounding. */
final class FinanceParserUtil {
    private FinanceParserUtil() {
    }

    /** Parses dollars with an optional leading dollar sign and at most two decimal places. */
    static BigDecimal parseAmount(String value) throws ParseException {
        if (!value.matches("\\$?[0-9]+(\\.[0-9]{1,2})?")) {
            throw new ParseException(Money.MESSAGE_CONSTRAINTS);
        }
        try {
            return Money.positive(new BigDecimal(value.replace("$", "")));
        } catch (IllegalArgumentException e) {
            throw new ParseException(Money.MESSAGE_CONSTRAINTS, e);
        }
    }
}
