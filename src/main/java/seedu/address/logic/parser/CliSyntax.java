package seedu.address.logic.parser;

import java.util.List;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /** The retired AddressBook address prefix. Use {@link #PREFIX_VENUE} instead. */
    public static final Prefix PREFIX_RETIRED_ADDRESS = new Prefix("a/");

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_LEVEL = new Prefix("l/");
    public static final Prefix PREFIX_SUBJECT = new Prefix("s/");
    public static final Prefix PREFIX_RATE = new Prefix("r/");
    public static final Prefix PREFIX_VENUE = new Prefix("v/");
    public static final Prefix PREFIX_REMARK = new Prefix("r/");
    public static final Prefix PREFIX_STUDENT = new Prefix("st/");
    public static final Prefix PREFIX_AMOUNT = new Prefix("amt/");
    public static final Prefix PREFIX_DATE = new Prefix("d/");
    public static final Prefix PREFIX_TIME = new Prefix("t/");
    public static final Prefix PREFIX_DURATION = new Prefix("dur/");
    public static final Prefix PREFIX_REASON = new Prefix("r/");
    public static final Prefix PREFIX_NOTES = new Prefix("n/");
    public static final Prefix PREFIX_DESCRIPTION = new Prefix("desc/");
    public static final Prefix PREFIX_DUE = new Prefix("due/");
    public static final Prefix PREFIX_LESSON = new Prefix("lesson/");
    public static final Prefix PREFIX_LESSON_INDEX = new Prefix("l/");
    public static final Prefix PREFIX_HOMEWORK = new Prefix("hw/");
    public static final Prefix PREFIX_PAYMENT = new Prefix("pay/");
    public static final Prefix PREFIX_WEEK = new Prefix("week/");
    public static final Prefix PREFIX_ALL = new Prefix("all/");
    public static final Prefix PREFIX_BUFFER = new Prefix("buffer/");
    public static final Prefix PREFIX_PAST = new Prefix("past/");
    public static final Prefix PREFIX_COUNT = new Prefix("count/");
    public static final Prefix PREFIX_EVERY = new Prefix("every/");
    public static final Prefix PREFIX_STATUS = new Prefix("s/");
    public static final Prefix PREFIX_FEEDBACK = new Prefix("f/");
    public static final Prefix PREFIX_OVERDUE = new Prefix("overdue/");
    public static final Prefix PREFIX_OUTSTANDING = new Prefix("outstanding/");

    /** Every prefix understood by at least one command. */
    public static final List<Prefix> ALL_PREFIXES = List.of(
            PREFIX_RETIRED_ADDRESS, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_LEVEL, PREFIX_SUBJECT,
            PREFIX_RATE, PREFIX_VENUE, PREFIX_REMARK, PREFIX_STUDENT, PREFIX_AMOUNT, PREFIX_DATE, PREFIX_TIME,
            PREFIX_DURATION, PREFIX_REASON, PREFIX_NOTES, PREFIX_DESCRIPTION, PREFIX_DUE, PREFIX_LESSON,
            PREFIX_LESSON_INDEX, PREFIX_HOMEWORK, PREFIX_PAYMENT, PREFIX_WEEK, PREFIX_ALL, PREFIX_BUFFER,
            PREFIX_PAST, PREFIX_COUNT, PREFIX_EVERY, PREFIX_STATUS, PREFIX_FEEDBACK, PREFIX_OVERDUE,
            PREFIX_OUTSTANDING);

}
