package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class RateTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Rate(null));
    }

    @Test
    public void constructor_invalidRate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Rate(""));
        assertThrows(IllegalArgumentException.class, () -> new Rate("-5"));
    }

    @Test
    public void constructor_dollarSign_dollarSignIgnored() {
        assertEquals(new BigDecimal("62.50"), new Rate("$62.50").value);
    }

    @Test
    public void isValidRate() {
        // null rate
        assertThrows(NullPointerException.class, () -> Rate.isValidRate(null));

        // invalid rates
        assertFalse(Rate.isValidRate("")); // empty string
        assertFalse(Rate.isValidRate(" ")); // spaces only
        assertFalse(Rate.isValidRate("$")); // dollar sign only
        assertFalse(Rate.isValidRate("-1")); // negative
        assertFalse(Rate.isValidRate("abc")); // not a number
        assertFalse(Rate.isValidRate("12.345")); // three decimal places
        assertFalse(Rate.isValidRate("1,000")); // thousands separator
        assertFalse(Rate.isValidRate("$$50")); // two dollar signs
        assertFalse(Rate.isValidRate("10000")); // five digits
        assertFalse(Rate.isValidRate("5 0")); // space inside the number
        assertFalse(Rate.isValidRate(".5")); // no digits before the decimal point

        // valid rates
        assertTrue(Rate.isValidRate("0")); // zero is allowed for trial lessons
        assertTrue(Rate.isValidRate("45"));
        assertTrue(Rate.isValidRate("62.50"));
        assertTrue(Rate.isValidRate("62.5"));
        assertTrue(Rate.isValidRate("$45")); // leading dollar sign
        assertTrue(Rate.isValidRate(" 45 ")); // surrounding spaces
        assertTrue(Rate.isValidRate("9999.99")); // upper limit
    }

    @Test
    public void toDisplayString_formatsWithTwoDecimalPlaces() {
        assertEquals("$50.00", new Rate("50").toDisplayString());
        assertEquals("$62.50", new Rate("62.5").toDisplayString());
    }

    @Test
    public void toStringMethod_noDollarSign() {
        assertEquals("62.50", new Rate("$62.50").toString());
    }

    @Test
    public void equals() {
        Rate rate = new Rate("50");

        // same values -> returns true
        assertTrue(rate.equals(new Rate("50")));

        // same amount written with different decimal places -> returns true
        assertTrue(rate.equals(new Rate("$50.00")));

        // same object -> returns true
        assertTrue(rate.equals(rate));

        // null -> returns false
        assertFalse(rate.equals(null));

        // different types -> returns false
        assertFalse(rate.equals(5.0f));

        // different values -> returns false
        assertFalse(rate.equals(new Rate("60")));
    }

    @Test
    public void hashCode_equalRates_sameHashCode() {
        assertEquals(new Rate("50").hashCode(), new Rate("50.00").hashCode());
    }
}
