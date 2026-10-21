package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.model.lesson.Lesson;

/**
 * Panel containing the lessons that the lesson commands count from, numbered as those commands number them. It
 * shows what the last agenda or lesson list showed, so the numbers on screen are always the ones to type.
 */
public class LessonListPanel extends UiPart<Region> {
    public static final String MESSAGE_EMPTY = "No lessons are listed. Press Today or This week, or type "
            + "'lesson list st/1' for one student, to list lessons with numbers.";

    private static final String FXML = "LessonListPanel.fxml";

    @FXML
    private StackPane toolbarPlaceholder;
    @FXML
    private ListView<Lesson> lessonListView;

    /** Creates a panel for {@code lessons} whose buttons act through {@code actions}. */
    public LessonListPanel(ObservableList<Lesson> lessons, PanelActions actions) {
        super(FXML);
        requireNonNull(lessons);
        requireNonNull(actions);
        toolbarPlaceholder.getChildren().add(QuickCommand.toolbarOf(actions,
                new QuickCommand("Today", "agenda"), new QuickCommand("This week", "agenda week/")));
        lessonListView.setItems(lessons);
        lessonListView.setCellFactory(listView -> new LessonListViewCell(actions));
        Label empty = new Label(MESSAGE_EMPTY);
        empty.getStyleClass().add("panel-empty");
        empty.setWrapText(true);
        lessonListView.setPlaceholder(empty);
    }

    /** Returns the list that shows the lessons. */
    ListView<Lesson> getListView() {
        return lessonListView;
    }

    /** Custom {@code ListCell} that displays the graphics of a {@code Lesson} using a {@code LessonCard}. */
    static class LessonListViewCell extends ListCell<Lesson> {
        private final PanelActions actions;

        LessonListViewCell(PanelActions actions) {
            this.actions = actions;
        }

        @Override
        protected void updateItem(Lesson lesson, boolean empty) {
            super.updateItem(lesson, empty);

            if (empty || lesson == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new LessonCard(lesson, getIndex() + 1, actions).getRoot());
            }
        }
    }
}
