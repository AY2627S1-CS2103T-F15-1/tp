package seedu.address.model.lesson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class FlexibleDateParserTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 26);
    private static final LocalDate DEC_22_2026 = LocalDate.of(2026, 12, 22);

    private static void assertDates(LocalDate expected, String... inputs) {
        for (String input : inputs) {
            assertEquals(Optional.of(expected), FlexibleDateParser.parse(input, TODAY), input);
        }
    }

    private static void assertNotDates(String... inputs) {
        for (String input : inputs) {
            assertEquals(Optional.empty(), FlexibleDateParser.parse(input, TODAY), input);
        }
    }

    @Test
    public void parse_yearFirstWithFourDigitYear_success() {
        assertDates(DEC_22_2026, "2026-12-22", "2026/12/22", "2026.12.22", "2026 12 22", " 2026-12-22 ");
        assertDates(DEC_22_2026, "2026-Dec-22", "2026/dec/22", "2026 DEC 22", "2026-December-22");
        assertDates(LocalDate.of(2026, 9, 2), "2026-9-2", "2026-09-02", "2026-Sep-2", "2026-Sept-02");
    }

    @Test
    public void parse_dayFirstWithFourDigitYear_success() {
        assertDates(DEC_22_2026, "22/12/2026", "22-12-2026", "22.12.2026", "22 12 2026", "  22/12/2026  ");
        assertDates(DEC_22_2026, "22-Dec-2026", "22 Dec 2026", "22 december 2026", "22/DEC/2026", "22  Dec   2026");
        assertDates(LocalDate.of(2026, 9, 2), "2/9/2026", "02-09-2026", "2 Sep 2026", "2-SEPT-2026");
    }

    @Test
    public void parse_twoDigitYearAfterTheDay_isInThe2000s() {
        assertDates(DEC_22_2026, "22/12/26", "22-12-26", "22.12.26", "22 Dec 26", "22-dec-26", "22/Dec/26");
        assertDates(LocalDate.of(2000, 1, 1), "1/1/00");
        assertDates(LocalDate.of(2027, 1, 31), "31-01-27", "31 Jan 27");
    }

    @Test
    public void parse_twoDigitYearBeforeTheDay_isInThe2000s() {
        assertDates(DEC_22_2026, "26-12-22", "26/12/22", "26.12.22", "26-Dec-22", "26 DEC 22");
        assertDates(LocalDate.of(2027, 1, 31), "27-01-31", "27/Jan/31");
    }

    @Test
    public void parse_twoDigitsEitherWay_readsTheDateNearerToToday() {
        // 05/10/26 is 5 October 2026 or 26 October 2005 and the first is the nearer
        assertDates(LocalDate.of(2026, 10, 5), "05/10/26", "5-10-26", "05.10.26");
        // 12-10-26 is 12 October 2026 or 26 October 2012
        assertDates(LocalDate.of(2026, 10, 12), "12-10-26");
        // 26-12-22 is 22 December 2026 or 26 December 2022
        assertDates(DEC_22_2026, "26-12-22");
        assertEquals(Optional.of(LocalDate.of(2022, 12, 26)),
                FlexibleDateParser.parse("26-12-22", LocalDate.of(2022, 10, 1)));
    }

    @Test
    public void parse_twoDigitsEitherWayAtTheSameDistance_readsTheDayFirst() {
        // 01-01-02 is 1 January 2002 or 2 January 2001, and 3 July 2001 is as far from the one as from the other
        assertEquals(Optional.of(LocalDate.of(2002, 1, 1)),
                FlexibleDateParser.parse("01-01-02", LocalDate.of(2001, 7, 3)));
    }

    @Test
    public void parse_twoDigitYearThatCannotBeADay_readsYearFirst() {
        assertDates(LocalDate.of(2035, 12, 31), "35-12-31", "35/12/31");
        assertDates(LocalDate.of(2099, 1, 15), "99 Jan 15");
    }

    @Test
    public void parse_dayFirstNotARealDate_triesYearFirst() {
        // 31-04 is not a date in 2030, but the same digits read year first are 30 April 2031
        assertDates(LocalDate.of(2031, 4, 30), "31-04-30");
    }

    @Test
    public void parse_impossibleDates_empty() {
        assertNotDates("2026-02-30", "30/02/2026", "29/02/2027", "31-04-2026", "2026-13-01", "13/13/2026",
                "0/1/2026", "2026-00-10", "2026-Feb-30", "30 Feb 2026");
        assertDates(LocalDate.of(2028, 2, 29), "29 Feb 2028", "2028-02-29");
    }

    @Test
    public void parse_notInAnAcceptedWay_empty() {
        assertNotDates("", " ", "tomorrow", "today", "9/10", "2026", "2026-12", "22/12", "22/12/2026/1",
                "12/22/2026", "Dec 22 2026", "22-12/2026", "22/12-2026", "22--12--2026", "22 / 12 / 2026",
                "22/12/226", "22/12/20260", "222/12/26", "2026-1234-22", "22-Decx-2026", "22-D-2026",
                "22-Dece-2026", "22-12-2026x", "-22-12-26", "22-12-", "a-b-c");
    }

    @Test
    public void parse_nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> FlexibleDateParser.parse(null, TODAY));
        assertThrows(NullPointerException.class, () -> FlexibleDateParser.parse("22/12/2026", null));
    }
}
