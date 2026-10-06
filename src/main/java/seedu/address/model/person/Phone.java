package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS = "Phone numbers must contain 3 to 15 digits, optionally "
            + "starting with '+'. Spaces and hyphens between the digits are allowed.";
    public static final String VALIDATION_REGEX = "\\+?\\d{3,15}";
    private static final String INPUT_REGEX = "\\+?\\d[\\d -]*";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number. Spaces and hyphens are removed, so {@code 9123 4567} is stored as
     *              {@code 91234567}.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = normalize(phone);
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.matches(INPUT_REGEX) && normalize(test).matches(VALIDATION_REGEX);
    }

    private static String normalize(String phone) {
        return phone.replaceAll("[ -]", "");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
