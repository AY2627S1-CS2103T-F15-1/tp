package seedu.address.logic.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DetailViewTest {

    @Test
    public void line_factories_setTheFlags() {
        DetailView.Line plain = DetailView.Line.plain("a");
        DetailView.Line warning = DetailView.Line.warning("b");

        assertFalse(plain.isStruckThrough());
        assertFalse(plain.isWarning());
        assertFalse(warning.isStruckThrough());
        assertTrue(warning.isWarning());
        assertTrue(new DetailView.Line("c", true, false).isStruckThrough());
    }

    @Test
    public void constructors_nullPart_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DetailView.Line(null, false, false));
        assertThrows(NullPointerException.class, () -> new DetailView.Section(null, List.of()));
        assertThrows(NullPointerException.class, () -> new DetailView.Section("", null));
        assertThrows(NullPointerException.class, () -> new DetailView(null, "s", false, List.of()));
        assertThrows(NullPointerException.class, () -> new DetailView("t", null, false, List.of()));
        assertThrows(NullPointerException.class, () -> new DetailView("t", "s", false, null));
    }

    @Test
    public void sectionsAndLines_areCopied_soLaterChangesDoNotShow() {
        List<DetailView.Line> lines = new ArrayList<>(List.of(DetailView.Line.plain("a")));
        DetailView.Section section = new DetailView.Section("H", lines);
        List<DetailView.Section> sections = new ArrayList<>(List.of(section));
        DetailView view = new DetailView("t", "s", false, sections);

        lines.add(DetailView.Line.plain("b"));
        sections.clear();

        assertEquals(1, section.lines().size());
        assertEquals(1, view.sections().size());
        assertThrows(UnsupportedOperationException.class, () -> view.sections().clear());
        assertThrows(UnsupportedOperationException.class, () -> section.lines().clear());
    }

    @Test
    public void resultBoxText_isTheSummaryThenTheNoteThatTheContentIsInThePane() {
        assertEquals("Viewing: Amy\nShown in the detail pane.",
                new DetailView("Viewing: Amy", "Viewing: Amy", false, List.of()).resultBoxText());
    }

    @Test
    public void withNote_addsTheNoteBelowTheShownLineWithoutChangingTheOriginal() {
        DetailView view = new DetailView("T", "Summary", true, List.of());

        DetailView noted = view.withNote("Unsaved changes").withNote("Another note");

        assertEquals("Summary\nShown in the detail pane.", view.resultBoxText());
        assertEquals("Summary\nShown in the detail pane.\nUnsaved changes\nAnother note",
                noted.resultBoxText());
        assertEquals(List.of("Unsaved changes", "Another note"), noted.notes());
        assertTrue(noted.isLive());
        assertEquals("T", noted.title());
        assertThrows(NullPointerException.class, () -> view.withNote(null));
        assertThrows(NullPointerException.class, () -> new DetailView("t", "s", false, List.of(), null));
    }
}
