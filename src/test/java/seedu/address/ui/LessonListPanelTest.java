package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.ui.FxTestSupport.onFx;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import seedu.address.model.lesson.Lesson;
import seedu.address.testutil.LessonBuilder;

public class LessonListPanelTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        ObservableList<Lesson> lessons = FXCollections.observableArrayList();

        assertThrows(NullPointerException.class, () -> new LessonListPanel(null, new RecordingActions()));
        assertThrows(NullPointerException.class, () -> new LessonListPanel(lessons, null));
    }

    @Test
    public void panel_withNoLessons_saysHowToListThem() throws Exception {
        LessonListPanel panel = onFx(() -> new LessonListPanel(FXCollections.observableArrayList(),
                new RecordingActions()));

        onFx(() -> {
            Label placeholder = (Label) panel.getListView().getPlaceholder();
            assertEquals(LessonListPanel.MESSAGE_EMPTY, placeholder.getText());
            assertTrue(LessonListPanel.MESSAGE_EMPTY.contains("lesson list st/1"));
            return null;
        });
    }

    @Test
    public void panel_followsTheListItIsGiven() throws Exception {
        ObservableList<Lesson> lessons = FXCollections.observableArrayList();
        LessonListPanel panel = onFx(() -> new LessonListPanel(lessons, new RecordingActions()));

        onFx(() -> {
            lessons.add(new LessonBuilder().withStudent(ALICE).withDate(LocalDate.now().plusDays(1)).build());
            return null;
        });

        assertEquals(1, onFx(() -> panel.getListView().getItems().size()));
        assertTrue(onFx(() -> panel.getListView().getItems() == lessons));
    }

    @Test
    public void toolbar_hasTheTodayAndThisWeekButtonsThatRunTheAgenda() throws Exception {
        RecordingActions actions = new RecordingActions();
        LessonListPanel panel = onFx(() -> new LessonListPanel(FXCollections.observableArrayList(), actions));

        onFx(() -> {
            List<Button> buttons = panel.getRoot().lookupAll("." + QuickCommand.STYLE_CLASS).stream()
                    .map(node -> (Button) node).toList();
            assertEquals(List.of("Today", "This week"), buttons.stream().map(Button::getText).toList());
            buttons.forEach(Button::fire);
            return null;
        });

        assertEquals(List.of("run agenda", "run agenda week/"), actions.calls());
    }
}
