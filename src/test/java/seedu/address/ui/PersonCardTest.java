package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Tests the student card of the student list on the JavaFX application thread. */
public class PersonCardTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            // Another UI test started JavaFX earlier in the same test run
            started.countDown();
        }
        Platform.setImplicitExit(false);
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not start");
    }

    @Test
    public void constructor_archivedStudent_showsArchivedMarker() throws Exception {
        Label marker = archivedMarker(new PersonBuilder().withArchived(true).build());

        assertTrue(marker.isVisible());
        assertTrue(marker.isManaged());
        assertEquals("(archived)", marker.getText());
    }

    @Test
    public void constructor_activeStudent_hidesArchivedMarker() throws Exception {
        Label marker = archivedMarker(new PersonBuilder().build());

        assertFalse(marker.isVisible());
        assertFalse(marker.isManaged());
    }

    /** Returns the archived marker of a card built for {@code student} on the JavaFX application thread. */
    private static Label archivedMarker(Person student) throws Exception {
        FutureTask<Label> task = new FutureTask<>(() ->
                (Label) new PersonCard(student, 1).getRoot().lookup("#archived"));
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
