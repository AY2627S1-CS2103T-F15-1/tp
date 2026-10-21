package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.logic.Messages;
import seedu.address.model.lesson.Lesson;

/**
 * A UI component that displays a {@code Lesson} with the number that lesson commands use for it. A lesson that is
 * still to be taught also has buttons to reschedule or cancel it.
 */
public class LessonCard extends UiPart<Region> {
    public static final String STYLE_CLASS_STRUCK = "row-struck";

    private static final String FXML = "LessonListCard.fxml";

    @FXML
    private HBox cardPane;
    @FXML
    private Label number;
    @FXML
    private Label title;
    @FXML
    private Label status;
    @FXML
    private Label slot;
    @FXML
    private Label note;
    @FXML
    private HBox actions;

    /** Creates a card for {@code lesson}, which is number {@code displayedIndex} of the list, with its buttons. */
    public LessonCard(Lesson lesson, int displayedIndex, PanelActions panelActions) {
        super(FXML);
        requireNonNull(panelActions);
        requireNonNull(lesson);
        number.setText(displayedIndex + ". ");
        title.setText(lesson.getStudentName() + " — " + lesson.getSubject());
        status.setText(lesson.getStatus().toString().toLowerCase(Locale.ENGLISH));
        slot.setText(Messages.formatSlot(lesson));
        showIfPresent(note, lesson.getCancelReason().map(reason -> "Reason: " + reason)
                .or(() -> lesson.getNotes().map(text -> "Notes: " + text)).orElse(null));
        if (lesson.isCancelled()) {
            title.getStyleClass().add(STYLE_CLASS_STRUCK);
            slot.getStyleClass().add(STYLE_CLASS_STRUCK);
        }
        String label = "lesson " + displayedIndex + " with " + lesson.getStudentName();
        if (lesson.isScheduled()) {
            Runnable reschedule = () -> panelActions.prefill("lesson move " + displayedIndex + " ");
            Runnable cancel = () -> panelActions.ask("Cancel " + label + "?", "lesson cancel " + displayedIndex);
            actions.getChildren().add(new IconButton(Icon.EDIT, "Reschedule " + label, reschedule));
            actions.getChildren().add(new IconButton(Icon.CANCEL, "Cancel " + label, cancel));
        }
    }

    private static void showIfPresent(Label label, String text) {
        boolean isPresent = text != null;
        label.setText(isPresent ? text : "");
        label.setManaged(isPresent);
        label.setVisible(isPresent);
    }
}
