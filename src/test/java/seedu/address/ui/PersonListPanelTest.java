package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.ui.FxTestSupport.onFx;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import seedu.address.model.person.Person;

public class PersonListPanelTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @SuppressWarnings("unchecked")
    private static ListView<Person> listOf(PersonListPanel panel) {
        return (ListView<Person>) panel.getRoot().lookup("#personListView");
    }

    @Test
    public void constructor_nullActions_throwsNullPointerException() {
        ObservableList<Person> students = FXCollections.observableArrayList();
        Callable<PersonListPanel> create = () -> new PersonListPanel(students, null);

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> onFx(create));

        assertTrue(thrown.getCause() instanceof NullPointerException);
    }

    @Test
    public void panel_withNoStudents_saysHowToAddOne() throws Exception {
        PersonListPanel panel = onFx(() -> new PersonListPanel(FXCollections.observableArrayList(),
                new RecordingActions()));

        onFx(() -> {
            Label placeholder = (Label) listOf(panel).getPlaceholder();
            assertEquals(PersonListPanel.MESSAGE_EMPTY, placeholder.getText());
            assertTrue(placeholder.getText().contains("add n/NAME"));
            return null;
        });
    }

    @Test
    public void toolbar_hasTheActiveArchivedAndAllButtonsThatRunList() throws Exception {
        RecordingActions actions = new RecordingActions();
        PersonListPanel panel = onFx(() -> new PersonListPanel(FXCollections.observableArrayList(ALICE), actions));

        onFx(() -> {
            List<Button> buttons = panel.getRoot().lookupAll("." + QuickCommand.STYLE_CLASS).stream()
                    .map(node -> (Button) node).toList();
            assertEquals(List.of("Active", "Archived", "All"), buttons.stream().map(Button::getText).toList());
            buttons.forEach(Button::fire);
            return null;
        });

        assertEquals(List.of("run list", "run list archived/", "run list all/"), actions.calls());
    }
}
