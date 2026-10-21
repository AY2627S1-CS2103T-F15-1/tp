package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestSupport.onFx;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.shape.SVGPath;

public class IconButtonTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new IconButton(null, "d", () -> { }));
        assertThrows(NullPointerException.class, () -> new IconButton(Icon.EDIT, null, () -> { }));
        assertThrows(NullPointerException.class, () -> new IconButton(Icon.EDIT, "d", null));
    }

    @Test
    public void button_showsTheIconAndDescribesItselfByTooltipAndAccessibleText() throws Exception {
        IconButton button = onFx(() -> new IconButton(Icon.DELETE, "Delete student 1", () -> { }));

        onFx(() -> {
            assertTrue(button.getGraphic() instanceof SVGPath);
            assertTrue(button.getGraphic().getStyleClass().contains(Icon.STYLE_CLASS));
            assertTrue(button.getStyleClass().contains(IconButton.STYLE_CLASS));
            assertEquals("Delete student 1", button.getTooltip().getText());
            assertEquals("Delete student 1", button.getAccessibleText());
            assertEquals("", button.getText(), "a picture only: the text is for the screen reader");
            return null;
        });
    }

    @Test
    public void button_runsItsActionWhenPressed() throws Exception {
        AtomicInteger presses = new AtomicInteger();
        IconButton button = onFx(() -> new IconButton(Icon.EDIT, "Change", presses::incrementAndGet));

        onFx(() -> {
            button.fire();
            return null;
        });

        assertEquals(1, presses.get());
    }

    @Test
    public void icons_eachDrawAShapeThatIsNotSharedBetweenButtons() {
        for (Icon icon : Icon.values()) {
            SVGPath first = icon.shape();
            SVGPath second = icon.shape();

            assertNotNull(first.getContent());
            assertFalse(first.getContent().isBlank());
            assertFalse(first == second, "a node can have only one parent, so every button needs its own shape");
            assertEquals(first.getContent(), second.getContent());
        }
    }
}
