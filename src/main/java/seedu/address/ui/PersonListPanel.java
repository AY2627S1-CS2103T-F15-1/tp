package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    public static final String HIGHLIGHT_STYLE_CLASS = "highlighted-cell";
    public static final Duration HIGHLIGHT_DURATION = Duration.millis(2500);
    public static final String MESSAGE_EMPTY = "No students are listed. Add one with 'add n/NAME p/PHONE l/LEVEL "
            + "s/SUBJECT r/RATE', or press Archived or All to look further.";

    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private StackPane toolbarPlaceholder;
    @FXML
    private ListView<Person> personListView;

    private final Set<UUID> highlightedIds = new HashSet<>();
    private final PauseTransition highlightTimer = new PauseTransition(HIGHLIGHT_DURATION);

    private final PanelActions actions;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}, whose buttons act through
     * {@code actions}.
     */
    public PersonListPanel(ObservableList<Person> personList, PanelActions actions) {
        super(FXML);
        this.actions = requireNonNull(actions);
        toolbarPlaceholder.getChildren().add(QuickCommand.toolbarOf(actions, new QuickCommand("Active", "list"),
                new QuickCommand("Archived", "list archived/"), new QuickCommand("All", "list all/")));
        personListView.setItems(personList);
        Label empty = new Label(MESSAGE_EMPTY);
        empty.getStyleClass().add("panel-empty");
        empty.setWrapText(true);
        personListView.setPlaceholder(empty);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        highlightTimer.setOnFinished(event -> clearHighlight());
    }

    /**
     * Selects {@code students} in the list, scrolls the first of them into view and highlights them for
     * {@link #HIGHLIGHT_DURATION}. Students that the list does not show are left out.
     */
    public void highlight(List<Person> students) {
        requireNonNull(students);
        if (students.isEmpty()) {
            return;
        }
        highlightedIds.clear();
        personListView.getSelectionModel().clearSelection();
        int first = -1;
        for (Person student : students) {
            int index = indexOf(student);
            if (index >= 0) {
                highlightedIds.add(student.getId());
                personListView.getSelectionModel().select(index);
                first = first < 0 ? index : Math.min(first, index);
            }
        }
        if (first < 0) {
            personListView.refresh();
            return;
        }
        personListView.scrollTo(first);
        personListView.refresh();
        highlightTimer.playFromStart();
        logger.fine("Highlighted " + highlightedIds.size() + " new student(s)");
    }

    /** Returns the students that are highlighted at this moment. */
    public Set<UUID> getHighlightedIds() {
        return Set.copyOf(highlightedIds);
    }

    /** Returns the students that are selected at this moment. */
    public List<Person> getSelectedStudents() {
        return List.copyOf(personListView.getSelectionModel().getSelectedItems());
    }

    /** Returns how long a highlight lasts, which a test can shorten. */
    PauseTransition getHighlightTimer() {
        return highlightTimer;
    }

    private int indexOf(Person student) {
        for (int i = 0; i < personListView.getItems().size(); i++) {
            if (personListView.getItems().get(i).getId().equals(student.getId())) {
                return i;
            }
        }
        return -1;
    }

    private void clearHighlight() {
        highlightedIds.clear();
        personListView.refresh();
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);
            getStyleClass().remove(HIGHLIGHT_STYLE_CLASS);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1, actions).getRoot());
                if (highlightedIds.contains(person.getId())) {
                    getStyleClass().add(HIGHLIGHT_STYLE_CLASS);
                }
            }
        }
    }

}
