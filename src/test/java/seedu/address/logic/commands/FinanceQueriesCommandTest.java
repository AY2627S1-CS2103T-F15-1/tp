package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalPersons;

public class FinanceQueriesCommandTest {
    private final ModelManager model = new ModelManager();
    private final AddressBookParser parser = new AddressBookParser();

    private String run(String command) throws Exception {
        return parser.parseCommand(command).execute(model).getFeedbackToUser();
    }

    private void lesson(Person student, int daysAgo) {
        model.addLesson(new LessonBuilder().withStudent(student).withDate(LocalDate.now().minusDays(daysAgo)).build()
                .withStudent(student));
    }

    @Test
    public void dues_partialPayment_ordersByOldestRemainingChargeAndIncludesArchived() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(TypicalPersons.BENSON);
        lesson(TypicalPersons.ALICE, 40);
        lesson(TypicalPersons.ALICE, 10);
        lesson(TypicalPersons.BENSON, 20);
        run("charge l/1 amt/100");
        run("charge l/2 amt/5");
        run("charge l/3 amt/100");
        run("pay st/1 amt/150");
        run("pay list");
        List<LedgerEntry> shown = model.getDisplayedPayments();
        run("archive 2");
        String dues = run("dues");
        assertTrue(dues.indexOf("Benson") < dues.indexOf("Alice"));
        assertTrue(dues.contains("$55.00 total"));
        assertTrue(dues.contains("oldest unpaid 20 days"));
        assertTrue(dues.contains("oldest unpaid 10 days"));
        assertTrue(dues.contains("(archived)"));
        assertFalse(dues.contains("1."));
        assertTrue(run("view 1").contains("OUTSTANDING $50.00"));
        assertTrue(run("today").contains("$55.00 total"));
        assertEquals(shown, model.getDisplayedPayments());
        run("pay st/1 amt/80");
        assertFalse(run("dues").contains("Alice"));
        assertTrue(run("balance st/1").contains("IN CREDIT $30.00"));
    }

    @Test
    public void dues_chargeFromOneDayAgo_saysDayInTheSingular() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        lesson(TypicalPersons.ALICE, 1);
        run("charge l/1 amt/10");

        String dues = run("dues");

        assertTrue(dues.contains("oldest unpaid 1 day"));
        assertFalse(dues.contains("1 days"));
    }

    @Test
    public void account_sameDayReversals_showsEverySignedEventAndNetTotals() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        lesson(TypicalPersons.ALICE, 1);
        run("charge l/1 amt/100");
        run("pay st/1 amt/150 n/Bank transfer");
        run("pay list");
        run("charge reverse l/1");
        String credit = run("balance st/1");
        assertTrue(credit.contains("charge Math lesson $100.00 bal $100.00"));
        assertTrue(credit.contains("payment Bank transfer -$150.00 bal -$50.00"));
        assertTrue(credit.contains("reversal of charge"));
        assertTrue(credit.contains("Total charged $0.00"));
        assertTrue(credit.contains("total paid $150.00"));
        assertTrue(credit.contains("IN CREDIT $150.00"));
        run("pay reverse 1");
        String cleared = run("balance st/1");
        assertTrue(cleared.contains("reversal of payment"));
        assertTrue(cleared.contains("OUTSTANDING $0.00"));
        assertTrue(run("dues").contains("No outstanding balances"));
    }

    @Test
    public void account_overTwoHundredRows_doesNotTruncateOrChangeSelection() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        run("pay st/1 amt/1 n/First");
        run("pay list");
        List<LedgerEntry> shown = model.getDisplayedPayments();
        for (int i = 0; i < 205; i++) {
            run("pay st/1 amt/1 n/Transfer " + i);
        }
        String account = run("balance st/1");
        assertTrue(account.contains("Transfer 204"));
        assertTrue(account.contains("IN CREDIT $206.00"));
        assertEquals(206, account.lines().filter(line -> line.contains(" bal ")).count());
        assertEquals(shown, model.getDisplayedPayments());
    }

    @Test
    public void deleteAndClear_financialHistory_requiresConfirmationAndCascades() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        model.addPerson(TypicalPersons.BENSON);
        run("pay st/1 amt/10");
        run("pay st/2 amt/20");
        run("pay list");
        String prompt = run("delete 1");
        assertTrue(prompt.contains("financial"));
        assertTrue(prompt.contains("IN CREDIT $10.00"));
        assertEquals(2, model.getAddressBook().getPersonList().size());
        run("delete 1 confirm");
        assertEquals(1, model.getAddressBook().getLedgerEntries().size());
        assertThrows(CommandException.class, () -> run("pay reverse 1"));
        run("pay reverse 2");
        assertTrue(run("clear").contains("financial"));
        assertEquals(2, model.getAddressBook().getLedgerEntries().size());
        run("clear confirm");
        assertTrue(model.getAddressBook().getLedgerEntries().isEmpty());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void rendering_accountAndDues_useReadableSeparatorsWithoutReplacementCharacters() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        lesson(TypicalPersons.ALICE, 1);
        String charge = run("charge l/1 amt/100");
        run("pay st/1 amt/25 n/Bank transfer");
        String account = run("balance st/1");
        String dues = run("dues");
        assertTrue(charge.contains("Alice Pauline \u2014 Math"));
        assertTrue(account.startsWith("Account \u2014 Alice Pauline\n"));
        assertTrue(account.contains("$100.00 \u00b7 total paid $25.00 (after reversals) \u00b7 OUTSTANDING $75.00"));
        assertTrue(dues.contains("outstanding balances \u2014 $75.00 total"));
        for (String text : List.of(charge, account, dues, run("view 1"), run("today"))) {
            assertFalse(text.contains("\uFFFD"), "Financial output must not contain replacement characters");
        }
    }

    @Test
    public void emptyAndInvalidQueries_reportClearFeedback() throws Exception {
        model.addPerson(TypicalPersons.ALICE);
        assertTrue(run("balance st/1").contains("No charges or payments recorded"));
        assertTrue(run("dues").contains("No outstanding balances"));
        assertThrows(ParseException.class, () -> run("dues extra"));
        assertThrows(ParseException.class, () -> run("balance 1"));
        assertThrows(ParseException.class, () -> run("balance st/1 st/1"));
        assertThrows(CommandException.class, () -> run("balance st/2"));
        for (String topic : List.of("charge", "charge reverse", "pay", "pay list", "pay reverse", "balance", "dues")) {
            assertTrue(run("help " + topic).contains(topic));
        }
    }
}
