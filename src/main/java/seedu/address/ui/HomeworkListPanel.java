package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import seedu.address.logic.view.HomeworkRow;

/**
 * Panel containing the homework that the last {@code hw list} showed, numbered as the homework commands number it.
 * It is drawn again after every command, so a row shows the homework as it is and not as it was when listed.
 */
public class HomeworkListPanel extends UiPart<Region> {
    public static final String MESSAGE_EMPTY = "No homework is listed. Press All, Overdue or Outstanding, or type "
            + "'hw list', to list homework with numbers.";

    private static final String FXML = "HomeworkListPanel.fxml";

    private final ObservableList<HomeworkRow> rows = FXCollections.observableArrayList();

    @FXML
    private StackPane toolbarPlaceholder;
    @FXML
    private ListView<HomeworkRow> homeworkListView;

    /** Creates an empty panel whose buttons act through {@code actions}. */
    public HomeworkListPanel(PanelActions actions) {
        super(FXML);
        requireNonNull(actions);
        toolbarPlaceholder.getChildren().add(QuickCommand.toolbarOf(actions, new QuickCommand("All", "hw list"),
                new QuickCommand("Overdue", "hw list overdue/"),
                new QuickCommand("Outstanding", "hw list outstanding/")));
        homeworkListView.setItems(rows);
        homeworkListView.setCellFactory(listView -> new HomeworkListViewCell(actions));
        Label empty = new Label(MESSAGE_EMPTY);
        empty.getStyleClass().add("panel-empty");
        empty.setWrapText(true);
        homeworkListView.setPlaceholder(empty);
    }

    /** Draws {@code newRows} in place of the rows that the panel shows. */
    public void show(List<HomeworkRow> newRows) {
        requireNonNull(newRows);
        if (!rows.equals(newRows)) {
            rows.setAll(newRows);
        }
    }

    /** Returns the list that shows the rows. */
    ListView<HomeworkRow> getListView() {
        return homeworkListView;
    }

    /** Custom {@code ListCell} that displays the graphics of a {@code HomeworkRow} using a {@code HomeworkCard}. */
    static class HomeworkListViewCell extends ListCell<HomeworkRow> {
        private final PanelActions actions;

        HomeworkListViewCell(PanelActions actions) {
            this.actions = actions;
        }

        @Override
        protected void updateItem(HomeworkRow row, boolean empty) {
            super.updateItem(row, empty);

            if (empty || row == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new HomeworkCard(row, actions).getRoot());
            }
        }
    }
}
