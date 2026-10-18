package seedu.address.model.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Exact SGD amounts; input precision is checked, while computed charges round once at the end. */
public final class Money {
    public static final String MESSAGE_CONSTRAINTS = "Amount must be greater than 0 with at most 2 decimal places.";
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    private Money() {
    }

    /** Validates positive amounts without silently rounding user input. */
    public static BigDecimal positive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    /** Computes an hourly charge using exact multiplication followed by one HALF_UP rounding. */
    public static BigDecimal charge(BigDecimal rate, int minutes) {
        return rate.multiply(BigDecimal.valueOf(minutes)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    /** Formats money without locale-dependent separators. */
    public static String format(BigDecimal amount) {
        return "$" + amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }
}
