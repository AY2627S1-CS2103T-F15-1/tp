package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.logic.view.HomeworkRow;

/**
 * A UI component that displays a {@code HomeworkRow} with the number that homework commands use for it, and
 * buttons to change or delete the homework. A row for homework that has been deleted has no buttons.
 */
public class HomeworkCard extends UiPart<Region> {
    public static final String STYLE_CLASS_WARNING = "row-warning";
    public static final String STYLE_CLASS_STRUCK = "row-struck";

    private static final String FXML = "HomeworkListCard.fxml";

    @FXML
    private HBox cardPane;
    @FXML
    private Label number;
    @FXML
    private Label title;
    @FXML
    private Label student;
    @FXML
    private Label due;
    @FXML
    private Label feedback;
    @FXML
    private HBox actions;

    /** Creates a card for {@code row}, whose number is the one that the homework commands use, with its buttons. */
    public HomeworkCard(HomeworkRow row, PanelActions panelActions) {
        super(FXML);
        requireNonNull(panelActions);
        requireNonNull(row);
        number.setText(row.number() + ". ");
        title.setText(row.description());
        student.setText(row.student());
        due.setText("Due " + row.dueDate() + " — " + row.status());
        feedback.setText(row.feedback().map(text -> "Feedback: " + text).orElse(""));
        feedback.setManaged(row.feedback().isPresent());
        feedback.setVisible(row.feedback().isPresent());
        if (row.isOverdue()) {
            due.getStyleClass().add(STYLE_CLASS_WARNING);
        }
        if (row.isDeleted()) {
            title.getStyleClass().add(STYLE_CLASS_STRUCK);
            due.getStyleClass().add(STYLE_CLASS_STRUCK);
            return;
        }
        String label = "homework " + row.number() + ", " + row.description();
        String current = "hw edit " + row.number() + " desc/" + row.description() + " due/" + row.dueDate();
        Runnable change = () -> panelActions.prefill(current);
        Runnable delete = () -> panelActions.ask("Delete " + label + "?", "hw delete " + row.number());
        actions.getChildren().add(new IconButton(Icon.EDIT, "Change " + label, change));
        actions.getChildren().add(new IconButton(Icon.DELETE, "Delete " + label, delete));
    }
}
