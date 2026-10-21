package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.DemoCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.LessonAddCommand;
import seedu.address.logic.parser.AddressBookParser;

public class GuidedTourTest {
    private static final LocalDate TODAY = LocalDate.of(2030, 1, 7);

    @TempDir
    public Path temporaryFolder;

    private MainWindowFixture fixture;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @BeforeEach
    public void createFixture() {
        fixture = new MainWindowFixture(temporaryFolder);
    }

    @AfterEach
    public void closeWindow() throws Exception {
        fixture.close();
    }

    @Test
    public void steps_beginWithAWelcomeAndEndWithAnInvitationToReturn() {
        List<TourStep> steps = GuidedTour.steps(TODAY);

        assertEquals("Welcome to TutorFlow", steps.get(0).title());
        assertFalse(steps.get(0).hasTarget(), "the welcome points at nothing, so it sits in the middle");
        assertEquals("You are ready", steps.get(steps.size() - 1).title());
        assertTrue(steps.get(steps.size() - 1).text().contains("tour"), "the end says how to come back");
        assertTrue(steps.size() >= 8 && steps.size() <= 12, "a tour that is short enough to finish");
    }

    @Test
    public void steps_everyStepHasATitleAndATextAndNoTwoStepsShareATitle() {
        List<TourStep> steps = GuidedTour.steps(TODAY);

        steps.forEach(step -> {
            assertFalse(step.title().isBlank());
            assertFalse(step.text().isBlank());
        });
        assertEquals(steps.size(), steps.stream().map(TourStep::title).distinct().count());
    }

    @Test
    public void steps_everyExampleIsACommandThatTutorFlowAccepts() throws Exception {
        AddressBookParser parser = new AddressBookParser();

        for (TourStep step : GuidedTour.steps(TODAY)) {
            if (step.hasExample()) {
                assertNotNull(parser.parseCommand(step.example()), step.example());
                assertFalse(step.exampleLabel().isBlank());
            }
        }
    }

    @Test
    public void steps_examplesTeachTheCommandsThatTheTextNames() {
        List<String> examples = GuidedTour.steps(TODAY).stream().filter(TourStep::hasExample)
                .map(TourStep::example).toList();

        assertTrue(examples.contains(HelpCommand.COMMAND_WORD));
        assertTrue(examples.contains(DemoCommand.COMMAND_WORD));
        assertTrue(examples.stream().anyMatch(example -> example.startsWith(AddCommand.COMMAND_WORD + " ")));
        assertTrue(examples.stream().anyMatch(example -> example.startsWith(LessonAddCommand.COMMAND_WORD + " ")));
    }

    @Test
    public void steps_lessonExampleIsOnTheDayAfterToday() {
        String lesson = GuidedTour.steps(TODAY).stream().filter(TourStep::hasExample).map(TourStep::example)
                .filter(example -> example.startsWith(LessonAddCommand.COMMAND_WORD)).findFirst().orElseThrow();

        assertTrue(lesson.contains("d/" + TODAY.plusDays(1) + " "), lesson);
    }

    @Test
    public void steps_everyTargetIsAPartOfTheOpenWindow() throws Exception {
        MainWindow window = fixture.open("");

        for (TourStep step : GuidedTour.steps(TODAY)) {
            if (step.hasTarget()) {
                assertNotNull(fixture.find(step.target()), step.target() + " of " + step.title());
            }
        }
        assertNotNull(window);
    }
}
