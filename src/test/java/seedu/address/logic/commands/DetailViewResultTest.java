package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.view.DetailView;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.HomeworkBuilder;
import seedu.address.testutil.LessonBuilder;

/** Checks that today, agenda and view hand the detail pane the same content as their text, in structure. */
public class DetailViewResultTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 9);

    private final Model model = newModel();

    private static Model newModel() {
        AddressBook book = new AddressBook();
        book.addPerson(ALICE);
        book.addPerson(BENSON);
        return new ModelManager(book, new UserPrefs());
    }

    private static DetailView.Section section(DetailView view, String headingStart) {
        return view.sections().stream().filter(section -> section.heading().startsWith(headingStart)).findFirst()
                .orElseThrow(() -> new AssertionError("No section " + headingStart + " in " + view));
    }

    /** Asserts that every heading and line of {@code view} is also in {@code text}, so the two cannot disagree. */
    private static void assertSameContentAsText(DetailView view, String text) {
        assertTrue(text.contains(view.title()), view.title());
        for (DetailView.Section section : view.sections()) {
            assertTrue(section.heading().isEmpty() || text.contains(section.heading()), section.heading());
            for (DetailView.Line line : section.lines()) {
                assertTrue(text.contains(line.text()), line.text());
            }
        }
    }

    @Test
    public void today_attachesTheDashboardSectionsInOrder() {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(TODAY).withTime("16:30").build());
        model.addLesson(new LessonBuilder().withStudent(BENSON).withDate(TODAY.minusDays(1)).build());
        model.addHomework(new HomeworkBuilder(ALICE).withDescription("Algebra").withDueDate(TODAY.minusDays(2))
                .build());

        CommandResult result = new TodayCommand(TODAY).execute(model);
        DetailView view = result.getDetailView().orElseThrow();

        assertEquals("Wednesday, 9 January 2030", view.title());
        assertEquals(view.title(), view.summary());
        assertEquals(List.of("TODAY'S LESSONS (1, 1h 30m)", "LESSONS AWAITING NOTES (1)", "OVERDUE HOMEWORK (1)"),
                view.sections().stream().map(DetailView.Section::heading).toList());
        assertSameContentAsText(view, result.getFeedbackToUser());
    }

    @Test
    public void today_emptyData_giveEverySectionItsEmptyState() {
        DetailView view = new TodayCommand(TODAY).execute(model).getDetailView().orElseThrow();

        assertEquals("No lessons today.", section(view, "TODAY'S LESSONS").lines().getFirst().text());
        assertEquals("All past lessons are recorded.", section(view, "LESSONS AWAITING").lines().getFirst().text());
        assertEquals("No overdue homework.", section(view, "OVERDUE HOMEWORK").lines().getFirst().text());
    }

    @Test
    public void today_overlappingLessonsAndOverdueHomework_areDrawnAsWarnings() {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(TODAY).withTime("16:30").withDuration(90)
                .build());
        model.addLesson(new LessonBuilder().withStudent(BENSON).withDate(TODAY).withTime("17:00").build());
        model.addHomework(new HomeworkBuilder(ALICE).withDueDate(TODAY.minusDays(2)).build());
        model.addHomework(new HomeworkBuilder(BENSON).withDescription("Done").withDueDate(TODAY.minusDays(2))
                .withStatus(HomeworkStatus.SUBMITTED).build());

        DetailView view = new TodayCommand(TODAY).execute(model).getDetailView().orElseThrow();

        assertTrue(section(view, "TODAY'S LESSONS").lines().stream().allMatch(DetailView.Line::isWarning));
        assertEquals(1, section(view, "OVERDUE HOMEWORK").lines().size());
        assertTrue(section(view, "OVERDUE HOMEWORK").lines().getFirst().isWarning());
    }

    @Test
    public void agendaDay_drawsCancelledLessonsStruckThroughAndKeepsTheirNumbers() {
        Lesson kept = new LessonBuilder().withStudent(ALICE).withDate(TODAY).withTime("10:00").build();
        Lesson cancelled = new LessonBuilder().withStudent(BENSON).withDate(TODAY).withTime("12:00").build()
                .cancel(null);
        model.addLesson(kept);
        model.addLesson(cancelled);

        CommandResult result = new AgendaCommand(TODAY, false).execute(model);
        DetailView view = result.getDetailView().orElseThrow();
        List<DetailView.Line> lines = view.sections().getFirst().lines();

        assertEquals("Agenda — Wed 09 Jan 2030 (1 lesson, 1h 30m)", view.title());
        assertEquals(2, lines.size());
        assertFalse(lines.get(0).isStruckThrough());
        assertTrue(lines.get(1).isStruckThrough());
        assertTrue(lines.get(0).text().startsWith(" 1. 10:00-11:30"));
        assertTrue(lines.get(1).text().startsWith(" 2. 12:00-13:30"));
        assertSameContentAsText(view, result.getFeedbackToUser());
    }

    @Test
    public void agendaDay_noLessons_hasOnlyTheTitle() {
        DetailView view = new AgendaCommand(TODAY, false).execute(model).getDetailView().orElseThrow();

        assertEquals("No lessons on Wed 09 Jan 2030.", view.title());
        assertEquals(view.title(), view.summary());
        assertTrue(view.sections().isEmpty());
    }

    @Test
    public void agendaWeek_hasASectionForEveryDayFromMondayToSunday() {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(TODAY).withTime("10:00").build());

        CommandResult result = new AgendaCommand(TODAY, true).execute(model);
        DetailView view = result.getDetailView().orElseThrow();

        assertEquals("Week of Mon 07 Jan 2030 to Sun 13 Jan 2030 (1 lesson, 1h 30m)", view.title());
        assertEquals(7, view.sections().size());
        assertEquals("Mon 07 Jan 2030 (0 lessons, 0m)", view.sections().getFirst().heading());
        assertEquals("— no lessons —", view.sections().getFirst().lines().getFirst().text());
        assertEquals("Wed 09 Jan 2030 (1 lesson, 1h 30m)", view.sections().get(2).heading());
        assertSameContentAsText(view, result.getFeedbackToUser());
    }

    @Test
    public void view_attachesTheOverviewSectionsAndNamesTheStudent() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(TODAY.plusDays(1)).build());
        model.addHomework(new HomeworkBuilder(ALICE).withDescription("Algebra").build());

        CommandResult result = new ViewCommand(INDEX_FIRST_PERSON, TODAY).execute(model);
        DetailView view = result.getDetailView().orElseThrow();

        assertEquals("Viewing: Alice Pauline", view.title());
        assertEquals(List.of("", "NEXT LESSON", "RECENT LESSON NOTES", "LESSON HISTORY", "OUTSTANDING HOMEWORK (1)"),
                view.sections().stream().map(DetailView.Section::heading).toList());
        assertTrue(view.sections().getFirst().lines().getFirst().text().startsWith("Level S3"));
        assertEquals("No lesson notes yet.", section(view, "RECENT").lines().getFirst().text());
        assertSameContentAsText(view, result.getFeedbackToUser());
    }

    @Test
    public void resultBoxText_namesWhatWasShownAndWhereTheContentIs() {
        DetailView view = new AgendaCommand(TODAY, true).execute(model).getDetailView().orElseThrow();

        assertEquals("Week of Mon 07 Jan 2030 to Sun 13 Jan 2030 (0 lessons, 0m)\nShown in the detail pane.",
                view.resultBoxText());
    }

    @Test
    public void onlyTheDashboardIsLive() throws Exception {
        model.addLesson(new LessonBuilder().withStudent(ALICE).withDate(TODAY).build());

        assertTrue(new TodayCommand(TODAY).execute(model).getDetailView().orElseThrow().isLive());
        assertFalse(new AgendaCommand(TODAY, false).execute(model).getDetailView().orElseThrow().isLive());
        assertFalse(new AgendaCommand(TODAY, true).execute(model).getDetailView().orElseThrow().isLive());
        assertFalse(new ViewCommand(INDEX_FIRST_PERSON, TODAY).execute(model).getDetailView().orElseThrow()
                .isLive());
    }

    @Test
    public void withAppendedText_addsToTheTextAndToTheResultBoxOfTheView() {
        CommandResult result = new CommandResult("text").withDetailView(
                new DetailView("T", "S", false, List.of()));

        CommandResult appended = result.withAppendedText("\nExtra");

        assertEquals("text\nExtra", appended.getFeedbackToUser());
        assertEquals("S\nShown in the detail pane.\nExtra", appended.getDetailView().orElseThrow().resultBoxText());
        assertEquals(result.getDetailView(), result.withAppendedText("  ").getDetailView());
        assertEquals("plain\nExtra", new CommandResult("plain").withAppendedText("\nExtra").getFeedbackToUser());
        assertTrue(new CommandResult("plain").withAppendedText("\nExtra").getDetailView().isEmpty());
    }

    @Test
    public void otherCommands_haveNoDetailView() throws Exception {
        assertTrue(new ListCommand().execute(model).getDetailView().isEmpty());
        assertTrue(new HomeworkListCommand(null, false, false, TODAY).execute(model).getDetailView().isEmpty());
    }

    @Test
    public void withDetailView_keepsTheOtherPartsOfTheResult() {
        DetailView view = new DetailView("T", "S", false, List.of());
        CommandResult result = new CommandResult("text").withDisplayedLessons(List.of()).withDetailView(view);

        assertEquals(view, result.getDetailView().orElseThrow());
        assertTrue(result.getDisplayedLessons().isPresent());
        assertEquals("changed", result.withFeedback("changed").getFeedbackToUser());
        assertEquals(view, result.withFeedback("changed").getDetailView().orElseThrow());
        assertEquals(view, result.withDisplayedHomework(List.of()).getDetailView().orElseThrow());
    }

    @Test
    public void addedStudents_arePassedOnAndDefaultToNone() throws Exception {
        assertTrue(new CommandResult("x").getAddedStudents().isEmpty());
        CommandResult result = new AddCommand(CARL).execute(model);

        assertEquals(List.of(CARL), result.getAddedStudents());
        assertEquals(result.getAddedStudents(), result.withFeedback("again").getAddedStudents());
        assertEquals(result.getAddedStudents(), result.withDetailView(new DetailView("T", "S", false, List.of()))
                .getAddedStudents());
    }
}
