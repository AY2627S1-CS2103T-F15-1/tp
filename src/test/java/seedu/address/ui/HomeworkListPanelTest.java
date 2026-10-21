package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestSupport.onFx;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import seedu.address.logic.view.HomeworkRow;

public class HomeworkListPanelTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    private static HomeworkRow row(int number, String description) {
        return new HomeworkRow(number, "Alice Pauline", description, LocalDate.of(2030, 1, 12), "assigned", false,
                Optional.empty(), false);
    }

    @Test
    public void constructor_nullActions_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HomeworkListPanel(null));
    }

    @Test
    public void show_nullRows_throwsNullPointerException() throws Exception {
        HomeworkListPanel panel = onFx(() -> new HomeworkListPanel(new RecordingActions()));

        assertThrows(NullPointerException.class, () -> panel.show(null));
    }

    @Test
    public void panel_withNoRows_saysHowToListHomework() throws Exception {
        HomeworkListPanel panel = onFx(() -> new HomeworkListPanel(new RecordingActions()));

        onFx(() -> {
            Label placeholder = (Label) panel.getListView().getPlaceholder();
            assertEquals(HomeworkListPanel.MESSAGE_EMPTY, placeholder.getText());
            assertTrue(HomeworkListPanel.MESSAGE_EMPTY.contains("hw list"));
            return null;
        });
    }

    @Test
    public void show_replacesTheRowsAndKeepsTheSameListWhenNothingChanged() throws Exception {
        HomeworkListPanel panel = onFx(() -> new HomeworkListPanel(new RecordingActions()));

        onFx(() -> {
            panel.show(List.of(row(1, "Algebra"), row(2, "Fractions")));
            assertEquals(List.of("Algebra", "Fractions"), panel.getListView().getItems().stream()
                    .map(HomeworkRow::description).toList());
            panel.show(List.of(row(1, "Algebra")));
            assertEquals(1, panel.getListView().getItems().size());
            panel.show(List.of());
            assertEquals(0, panel.getListView().getItems().size());
            return null;
        });
    }

    @Test
    public void toolbar_hasTheAllOverdueAndOutstandingButtonsThatRunHwList() throws Exception {
        RecordingActions actions = new RecordingActions();
        HomeworkListPanel panel = onFx(() -> new HomeworkListPanel(actions));

        onFx(() -> {
            List<Button> buttons = panel.getRoot().lookupAll("." + QuickCommand.STYLE_CLASS).stream()
                    .map(node -> (Button) node).toList();
            assertEquals(List.of("All", "Overdue", "Outstanding"), buttons.stream().map(Button::getText).toList());
            buttons.forEach(Button::fire);
            return null;
        });

        assertEquals(List.of("run hw list", "run hw list overdue/", "run hw list outstanding/"), actions.calls());
    }
}
