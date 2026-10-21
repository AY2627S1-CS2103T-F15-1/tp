package seedu.address.ui;

import java.time.LocalDate;
import java.util.List;

import seedu.address.logic.commands.AgendaCommand;
import seedu.address.logic.commands.DemoCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.TourCommand;

/**
 * The steps of the guided tour, in the order that they are shown. Every step points at a part of the window or
 * offers a command to try, and none of them changes data: an example is only put in the command box.
 */
public final class GuidedTour {
    public static final String COMMAND_BOX = "#commandTextField";
    public static final String PANEL_TABS = ".panel-tabs";
    public static final String DETAIL_PANE = ".detail-pane";
    public static final String RESULT_BOX = "#resultDisplay";
    public static final String STATUS_BAR = ".status-bar";

    private GuidedTour() {
    }

    /** Returns the steps of the tour, with examples that are valid on {@code today}. */
    public static List<TourStep> steps(LocalDate today) {
        return List.of(
                TourStep.of("Welcome to TutorFlow",
                        "TutorFlow keeps your students, lessons and homework in one place. This short tour shows "
                                + "you around, and nothing in it changes your data unless you press Enter "
                                + "yourself. Press Esc or Skip tour to leave at any time."),
                TourStep.of("Type commands here",
                        "Everything starts in the command box. Type a command and press Enter. Type help to see "
                                + "every command.")
                        .pointingAt(COMMAND_BOX).withExample("Put help in the command box",
                                HelpCommand.COMMAND_WORD),
                TourStep.of("Add your first student",
                        "A student has a name, phone, level, subject and hourly rate. Put the example in the "
                                + "command box, change it to your student's details and press Enter.")
                        .pointingAt(COMMAND_BOX).withExample("Put an example in the command box",
                                "add n/Alex Tan p/91234567 l/S3 s/Math r/50"),
                TourStep.of("Students, lessons and homework",
                        "Each tab lists its records with numbers. Use a number in a command, such as view 1 or "
                                + "edit 1. The buttons on a row change, archive or delete it, and always ask "
                                + "before they delete. Ctrl+1, Ctrl+2 and Ctrl+3 switch tabs.")
                        .pointingAt(PANEL_TABS),
                TourStep.of("Schedule a lesson",
                        "A lesson needs a student from the list, a date and a start time. Once it exists, "
                                + AgendaCommand.COMMAND_WORD + " lists the lessons of today in the Lessons tab.")
                        .pointingAt(COMMAND_BOX).withExample("Put an example in the command box",
                                "lesson add st/1 d/" + today.plusDays(1) + " t/16:30"),
                TourStep.of("Today at a glance",
                        "This pane shows what needs doing today. Commands such as agenda, view 1 and today draw "
                                + "their answer here, and it stays up to date as you work.")
                        .pointingAt(DETAIL_PANE),
                TourStep.of("Answers appear here",
                        "Every command answers in this box, and an error says what to fix. Nothing is deleted "
                                + "without asking first.")
                        .pointingAt(RESULT_BOX),
                TourStep.of("Your data is safe",
                        "TutorFlow saves after every change, to the file named here. If saving fails it tells "
                                + "you and keeps your changes until you type save.")
                        .pointingAt(STATUS_BAR),
                TourStep.of("Look around with sample data",
                        "Type demo to see what the demo loads, and demo confirm to load sample students. Your own "
                                + "data is kept safe and nothing in the demo is saved. Press Exit demo on the "
                                + "orange strip, or type demo exit, to go back to it.")
                        .pointingAt(COMMAND_BOX).withExample("Put demo in the command box",
                                DemoCommand.COMMAND_WORD),
                TourStep.of("You are ready",
                        "Type help any time to see every command, and " + TourCommand.COMMAND_WORD
                                + " to see this tour again."));
    }
}
