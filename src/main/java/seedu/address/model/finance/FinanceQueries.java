package seedu.address.model.finance;

import java.math.BigDecimal;
import java.util.UUID;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/** Read-only finance presentation shared by payment feedback, accounts, and daily summaries. */
public final class FinanceQueries {
    private FinanceQueries() {
    }

    /** Formats credit as a positive amount with an explicit label. */
    public static String balanceLabel(BigDecimal balance) {
        return balance.signum() < 0 ? "IN CREDIT " + Money.format(balance.negate())
                : "OUTSTANDING " + Money.format(balance);
    }

    /** Uses the student's current display identity without changing any financial snapshot. */
    public static String studentLabel(ReadOnlyAddressBook book, UUID studentId) {
        Person student = book.getPersonList().stream().filter(person -> person.getId().equals(studentId))
                .findFirst().orElseThrow();
        return student.getName() + (student.isArchived() ? " (archived)" : "");
    }
}
