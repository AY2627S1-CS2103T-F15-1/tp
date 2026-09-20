package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;

/**
 * Represents a student's lesson rate in TutorFlow.
 * Guarantees: immutable; is valid as declared in {@link #isValidRate(String)}
 *
 * <p>The rate is a non-negative amount with at most 2 decimal places, ranging
 * from 0 to 9999.99 inclusive. A rate of 0 is legitimate (trial or make-up lesson).
 * An optional leading {@code $} sign is accepted and stripped.
 */
public class Rate {

    public static final String MESSAGE_CONSTRAINTS =
            "Rate must be between 0 and 9999.99 with at most 2 decimal places, e.g. 45 or 62.50.";

    /**
     * Matches an optional dollar sign, then a number with at most 2 decimal places.
     */
    public static final String VALIDATION_REGEX = "\\$?\\d{1,4}(\\.\\d{1,2})?";

    private static final BigDecimal MAX_RATE = new BigDecimal("9999.99");

    public final BigDecimal value;

    /**
     * Constructs a {@code Rate}.
     *
     * @param rate A valid rate string.
     */
    public Rate(String rate) {
        requireNonNull(rate);
        String cleaned = rate.replace("$", "").strip();
        checkArgument(isValidRate(cleaned), MESSAGE_CONSTRAINTS);
        value = new BigDecimal(cleaned);
    }

    /**
     * Returns true if a given string is a valid rate.
     */
    public static boolean isValidRate(String test) {
        String cleaned = test.replace("$", "").strip();
        if (!cleaned.matches("\\d{1,4}(\\.\\d{1,2})?")) {
            return false;
        }
        BigDecimal amount = new BigDecimal(cleaned);
        return amount.compareTo(BigDecimal.ZERO) >= 0 && amount.compareTo(MAX_RATE) <= 0;
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

        return value.compareTo(otherRate.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }
}
