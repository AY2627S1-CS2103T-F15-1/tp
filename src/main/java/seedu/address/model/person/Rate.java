package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;

/**
 * Represents a student's lesson rate in TutorFlow, in dollars.
 * Guarantees: immutable; is valid as declared in {@link #isValidRate(String)}
 *
 * <p>The rate is a non-negative amount with at most 2 decimal places, ranging
 * from 0 to 9999.99 inclusive. A rate of 0 is legitimate (trial or make-up lesson).
 * One optional leading {@code $} sign is accepted and stripped.
 */
public class Rate {

    public static final String MESSAGE_CONSTRAINTS =
            "Rate must be between 0 and 9999.99 with at most 2 decimal places, e.g. 45 or 62.50.";

    /**
     * Matches an optional dollar sign, then a number of at most 4 digits before the decimal point and
     * at most 2 after it, which limits the rate to 0-9999.99.
     */
    public static final String VALIDATION_REGEX = "\\$?\\d{1,4}(\\.\\d{1,2})?";

    public final BigDecimal value;

    /**
     * Constructs a {@code Rate}.
     *
     * @param rate A valid rate string.
     */
    public Rate(String rate) {
        requireNonNull(rate);
        checkArgument(isValidRate(rate), MESSAGE_CONSTRAINTS);
        value = new BigDecimal(stripDollarSign(rate));
    }

    /**
     * Returns true if a given string is a valid rate.
     */
    public static boolean isValidRate(String test) {
        requireNonNull(test);
        return test.strip().matches(VALIDATION_REGEX);
    }

    private static String stripDollarSign(String rate) {
        String trimmed = rate.strip();
        return trimmed.startsWith("$") ? trimmed.substring(1) : trimmed;
    }

    /**
     * Returns the rate formatted as a dollar amount with 2 decimal places.
     */
    public String toDisplayString() {
        return String.format("$%.2f", value);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Rate otherRate)) {
            return false;
        }

        // compareTo ignores the scale, so that 50 and 50.00 are the same rate
        return value.compareTo(otherRate.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }
}
