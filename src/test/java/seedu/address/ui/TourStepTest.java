package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TourStepTest {

    @Test
    public void constructor_nullPart_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TourStep(null, "t", "", "", ""));
        assertThrows(NullPointerException.class, () -> new TourStep("h", null, "", "", ""));
        assertThrows(NullPointerException.class, () -> new TourStep("h", "t", null, "", ""));
        assertThrows(NullPointerException.class, () -> new TourStep("h", "t", "", null, ""));
        assertThrows(NullPointerException.class, () -> new TourStep("h", "t", "", "", null));
    }

    @Test
    public void of_pointsAtNothingAndHasNoExample() {
        TourStep step = TourStep.of("Title", "Text");

        assertEquals("Title", step.title());
        assertEquals("Text", step.text());
        assertFalse(step.hasTarget());
        assertFalse(step.hasExample());
    }

    @Test
    public void pointingAtAndWithExample_returnCopiesThatKeepTheRest() {
        TourStep plain = TourStep.of("Title", "Text");

        TourStep pointing = plain.pointingAt("#commandTextField");
        TourStep withExample = pointing.withExample("Try it", "help");

        assertFalse(plain.hasTarget());
        assertTrue(pointing.hasTarget());
        assertFalse(pointing.hasExample());
        assertEquals("#commandTextField", withExample.target());
        assertEquals("Try it", withExample.exampleLabel());
        assertEquals("help", withExample.example());
        assertTrue(withExample.hasExample());
        assertEquals("Title", withExample.title());
    }
}
