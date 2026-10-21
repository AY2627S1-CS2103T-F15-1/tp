package seedu.address.logic.view;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * The structured content of the detail pane: a title and sections of lines. Each line says how it is drawn, so
 * the pane never has to read text such as "(cancelled)" to decide that a lesson is struck through.
 *
 * @param title the first thing the pane shows, such as a date or the name of a student.
 * @param summary what the result box says about the content, which the pane shows in full.
 * @param isLive whether the pane redraws the content after every command, as it does for the dashboard, instead
 *               of leaving it as it was when it was requested.
 * @param sections the sections in the order that they are drawn.
 * @param notes lines that the result box adds below the summary, such as a warning about unsaved changes.
 */
public record DetailView(String title, String summary, boolean isLive, List<Section> sections,
        List<String> notes) {

    /** The line that follows the summary in the result box, as the content itself is in the detail pane. */
    public static final String MESSAGE_SHOWN_IN_PANE = "Shown in the detail pane.";

    /**
     * A heading with the lines under it.
     *
     * @param heading the heading, which is empty for lines that need none.
     * @param lines the lines of the section, in order.
     */
    public record Section(String heading, List<Line> lines) {

        /** Creates a section; {@code lines} is copied. */
        public Section {
            requireNonNull(heading);
            lines = List.copyOf(lines);
        }
    }

    /**
     * One line of text.
     *
     * @param text the text to draw, in a font with equal letter widths so that its columns line up.
     * @param isStruckThrough whether the line is drawn struck through, as a cancelled lesson is.
     * @param isWarning whether the line is drawn as a warning, as an overlapping lesson is.
     */
    public record Line(String text, boolean isStruckThrough, boolean isWarning) {

        /** Creates a line. */
        public Line {
            requireNonNull(text);
        }

        /** Returns a plain line. */
        public static Line plain(String text) {
            return new Line(text, false, false);
        }

        /** Returns a line drawn as a warning. */
        public static Line warning(String text) {
            return new Line(text, false, true);
        }
    }

    /** Creates a view; {@code sections} and {@code notes} are copied. */
    public DetailView {
        requireNonNull(title);
        requireNonNull(summary);
        sections = List.copyOf(sections);
        notes = List.copyOf(notes);
    }

    /** Creates a view without notes. */
    public DetailView(String title, String summary, boolean isLive, List<Section> sections) {
        this(title, summary, isLive, sections, List.of());
    }

    /** Returns this view with {@code note} added to what the result box shows. */
    public DetailView withNote(String note) {
        requireNonNull(note);
        List<String> allNotes = new ArrayList<>(notes);
        allNotes.add(note);
        return new DetailView(title, summary, isLive, sections, allNotes);
    }

    /** Returns what the result box shows when this view is in the detail pane. */
    public String resultBoxText() {
        StringBuilder text = new StringBuilder(summary).append("\n").append(MESSAGE_SHOWN_IN_PANE);
        notes.forEach(note -> text.append("\n").append(note));
        return text.toString();
    }
}
