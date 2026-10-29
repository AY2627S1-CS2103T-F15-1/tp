package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.TouchPoint;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/** Tests command-assisted student card controls on the JavaFX application thread. */
public class StudentCardActionsTest {
    private final StubCommandInput input = new StubCommandInput();
    private final List<String> help = new ArrayList<>();
    private final CommandAssist assist = new CommandAssist(input, help::add);
    private final Person student = new PersonBuilder().build();

    @BeforeAll
    public static void startJavaFx() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
        } catch (IllegalStateException alreadyStarted) {
            started.countDown();
        }
        Platform.setImplicitExit(false);
        assertTrue(started.await(10, TimeUnit.SECONDS), "JavaFX did not start");
    }

    @Test
    public void hoverControls_areHiddenUntilHoverAndCreateNoCommand() throws Exception {
        onFx(() -> {
            Node root = new PersonCard(student, 2, assist).getRoot();
            Button edit = (Button) root.lookup("#cardEditButton");
            assertFalse(edit.isVisible());

            root.fireEvent(mouseEntered(root));

            assertTrue(edit.isVisible());
            assertEquals("", input.getText());
        });
    }

    @Test
    public void keyboardReachability_cardAndControlsAreFocusTraversable() throws Exception {
        onFx(() -> {
            Node root = new PersonCard(student, 2, assist).getRoot();
            Button archive = (Button) root.lookup("#cardArchiveButton");

            assertTrue(root.isFocusTraversable());
            assertTrue(archive.isFocusTraversable());
        });
    }

    @Test
    public void touch_revealsCardControlsWithoutUsingHover() throws Exception {
        onFx(() -> {
            Node root = new PersonCard(student, 2, assist).getRoot();
            Button edit = (Button) root.lookup("#cardEditButton");
            assertFalse(edit.isVisible());

            root.fireEvent(touchPressed(root));

            assertTrue(edit.isVisible());
        });
    }

    @Test
    public void cardControls_populateCorrectDraftWithoutExecutingAnything() throws Exception {
        onFx(() -> {
            Node root = new PersonCard(student, 2, assist).getRoot();
            Button archive = (Button) root.lookup("#cardArchiveButton");
            archive.fire();
            assertEquals("archive 2", input.getText());
            Button delete = (Button) root.lookup("#cardDeleteButton");
            delete.fire();
            assertEquals("delete 2", input.getText());
        });
    }

    @Test
    public void editMode_revealsFieldsAndCancelClearsOnlyGeneratedDraft() throws Exception {
        onFx(() -> {
            Node root = new PersonCard(student, 2, assist).getRoot();
            Button edit = (Button) root.lookup("#cardEditButton");
            Button phone = (Button) root.lookup("#phoneEditButton");
            Button cancel = (Button) root.lookup("#cancelEditButton");

            edit.fire();
            assertTrue(phone.isVisible());
            assertTrue(cancel.isVisible());
            phone.fire();
            assertEquals("edit 2 p/", input.getText());
            input.setTextAndFocus("edit 2 p/91234567");
            phone.fire();
            assertEquals("edit 2 p/", input.getText());
            cancel.fire();
            assertEquals("", input.getText());
            assertFalse(phone.isVisible());
        });
    }

    @Test
    public void cancel_afterAnotherCardActionKeepsThatActionDraft() throws Exception {
        onFx(() -> {
            Node root = new PersonCard(student, 2, assist).getRoot();
            Button edit = (Button) root.lookup("#cardEditButton");
            Button archive = (Button) root.lookup("#cardArchiveButton");
            Button cancel = (Button) root.lookup("#cancelEditButton");

            edit.fire();
            archive.fire();
            cancel.fire();

            assertEquals("archive 2", input.getText());
        });
    }

    @Test
    public void editingAnotherCard_closesTheEarlierCardEditMode() throws Exception {
        onFx(() -> {
            PersonCard firstCard = new PersonCard(student, 1, assist);
            PersonCard secondCard = new PersonCard(student, 2, assist);
            Node firstRoot = firstCard.getRoot();
            Node secondRoot = secondCard.getRoot();
            Button firstEdit = (Button) firstRoot.lookup("#cardEditButton");
            Button firstPhone = (Button) firstRoot.lookup("#phoneEditButton");
            Button secondEdit = (Button) secondRoot.lookup("#cardEditButton");
            Button secondPhone = (Button) secondRoot.lookup("#phoneEditButton");

            firstEdit.fire();
            assertTrue(firstPhone.isVisible());

            secondEdit.fire();

            assertFalse(firstPhone.isVisible());
            assertTrue(secondPhone.isVisible());
        });
    }

    @Test
    public void displayedListChange_closesEditModeAndClearsUnchangedDraft() throws Exception {
        onFx(() -> {
            ObservableList<Person> people = FXCollections.observableArrayList(student);
            new PersonListPanel(people, assist);
            AtomicBoolean isClosed = new AtomicBoolean();
            assist.beginEditInteraction(() -> isClosed.set(true));
            assist.editField(1, CommandAssist.Field.NAME);

            people.set(0, new PersonBuilder().withName("Another Student").build());

            assertTrue(isClosed.get());
            assertEquals("", input.getText());
        });
    }

    private <T> void onFx(FxRunnable runnable) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            runnable.run();
            return null;
        });
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }

    private static MouseEvent mouseEntered(Node node) {
        return new MouseEvent(node, node, MouseEvent.MOUSE_ENTERED, 0, 0, 0, 0, MouseButton.NONE, 0,
                false, false, false, false, false, false, false, false, false, false, null);
    }

    private static TouchEvent touchPressed(Node node) {
        TouchPoint point = new TouchPoint(0, TouchPoint.State.PRESSED, 0, 0, 0, 0, node, null);
        return new TouchEvent(node, node, TouchEvent.TOUCH_PRESSED, point, List.of(point), 0,
                false, false, false, false);
    }

    private interface FxRunnable {
        void run();
    }

    private static final class StubCommandInput implements CommandDraftTarget {
        private String text = "";

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void setTextAndFocus(String text) {
            this.text = text;
        }

        @Override
        public void clearAndFocus() {
            text = "";
        }
    }
}
