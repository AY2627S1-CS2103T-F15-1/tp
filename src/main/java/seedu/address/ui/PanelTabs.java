package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

/**
 * The tabs beside the detail pane that hold the lists of students, lessons and homework. Each tab holds a list
 * whose rows carry the numbers that the commands use, so a tutor can read a number off the screen and type it.
 */
public class PanelTabs extends UiPart<TabPane> {

    /** The lists that the tabs hold, in the order of the tabs. */
    public enum Panel { STUDENTS, LESSONS, HOMEWORK }

    private static final String FXML = "PanelTabs.fxml";

    @FXML
    private Tab studentsTab;
    @FXML
    private Tab lessonsTab;
    @FXML
    private Tab homeworkTab;
    @FXML
    private StackPane studentsPlaceholder;
    @FXML
    private StackPane lessonsPlaceholder;
    @FXML
    private StackPane homeworkPlaceholder;

    /** Creates tabs that hold the three lists. */
    public PanelTabs(PersonListPanel students, LessonListPanel lessons, HomeworkListPanel homework) {
        super(FXML);
        studentsPlaceholder.getChildren().add(requireNonNull(students).getRoot());
        lessonsPlaceholder.getChildren().add(requireNonNull(lessons).getRoot());
        homeworkPlaceholder.getChildren().add(requireNonNull(homework).getRoot());
    }

    /** Shows the tab of {@code panel}. */
    public void select(Panel panel) {
        requireNonNull(panel);
        getRoot().getSelectionModel().select(tabOf(panel));
    }

    /** Returns the list that the selected tab shows. */
    public Panel getSelected() {
        Tab selected = getRoot().getSelectionModel().getSelectedItem();
        if (selected == lessonsTab) {
            return Panel.LESSONS;
        }
        return selected == homeworkTab ? Panel.HOMEWORK : Panel.STUDENTS;
    }

    private Tab tabOf(Panel panel) {
        return switch (panel) {
            case STUDENTS -> studentsTab;
            case LESSONS -> lessonsTab;
            case HOMEWORK -> homeworkTab;
        };
    }
}
