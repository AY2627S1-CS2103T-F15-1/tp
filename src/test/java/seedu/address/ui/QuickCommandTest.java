package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestSupport.onFx;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

public class QuickCommandTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @Test
    public void constructor_nullPart_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new QuickCommand(null, "list"));
        assertThrows(NullPointerException.class, () -> new QuickCommand("All", null));
    }

    @Test
    public void toButton_runsTheCommandAndNamesItInTheTooltip() throws Exception {
        RecordingActions actions = new RecordingActions();

        Button button = onFx(() -> {
            Button created = new QuickCommand("Overdue", "hw list overdue/").toButton(actions);
            created.fire();
            return created;
        });

        assertEquals(List.of("run hw list overdue/"), actions.calls());
        assertEquals("Overdue", button.getText());
        assertEquals("Same as typing: hw list overdue/", button.getTooltip().getText());
        assertEquals("Overdue. Same as typing: hw list overdue/", button.getAccessibleText());
        assertTrue(button.getStyleClass().contains(QuickCommand.STYLE_CLASS));
    }

    @Test
    public void toolbarOf_hasOneButtonForEachCommandInOrder() throws Exception {
        FlowPane toolbar = onFx(() -> QuickCommand.toolbarOf(new RecordingActions(),
                new QuickCommand("Active", "list"), new QuickCommand("All", "list all/")));

        assertEquals(List.of("Active", "All"),
                toolbar.getChildren().stream().map(node -> ((Button) node).getText()).toList());
    }

    @Test
    public void toButton_nullActions_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new QuickCommand("All", "list").toButton(null));
    }
}
