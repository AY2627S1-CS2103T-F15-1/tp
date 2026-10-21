package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seedu.address.logic.view.DetailView;

/**
 * The scrollable pane beside the student list that shows the dashboard, an agenda or a student overview. It
 * draws a {@link DetailView}, whose lines say how they are drawn, and never reads the text of a line to decide.
 */
public class DetailPane extends UiPart<Region> {

    public static final String TITLE_STYLE_CLASS = "detail-title";
    public static final String HEADING_STYLE_CLASS = "detail-heading";
    public static final String LINE_STYLE_CLASS = "detail-line";
    public static final String WARNING_STYLE_CLASS = "detail-warning";
    public static final String STRUCK_STYLE_CLASS = "detail-struck";

    private static final String FXML = "DetailPane.fxml";
    private static final double PAGE_FRACTION = 0.9;
    private static final double LINE_HEIGHT = 20;
    private static final int LINES_PER_STEP = 3;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox content;

    private DetailView shown;

    /** Creates an empty pane. */
    public DetailPane() {
        super(FXML);
    }

    /** Draws {@code view} from its top, replacing what the pane shows. */
    public void show(DetailView view) {
        draw(view);
        scrollPane.setVvalue(0);
    }

    /** Draws {@code view} in place of what the pane shows, and stays where the user has scrolled to. */
    public void refresh(DetailView view) {
        double position = scrollPane.getVvalue();
        draw(view);
        scrollPane.setVvalue(position);
    }

    /** Returns what the pane shows, if it shows anything. */
    public Optional<DetailView> getShown() {
        return Optional.ofNullable(shown);
    }

    /** Scrolls down, or up if {@code isDown} is false, by most of a screen. */
    public void scrollPage(boolean isDown) {
        double viewport = scrollPane.getViewportBounds().getHeight();
        scrollBy((isDown ? 1 : -1) * viewport * PAGE_FRACTION);
    }

    /** Scrolls down, or up if {@code isDown} is false, by a few lines. */
    public void scrollLines(boolean isDown) {
        scrollBy((isDown ? 1 : -1) * LINES_PER_STEP * LINE_HEIGHT);
    }

    private void scrollBy(double pixels) {
        double scrollable = content.getLayoutBounds().getHeight() - scrollPane.getViewportBounds().getHeight();
        if (scrollable <= 0) {
            return;
        }
        scrollPane.setVvalue(Math.max(0, Math.min(1, scrollPane.getVvalue() + pixels / scrollable)));
    }

    private void draw(DetailView view) {
        requireNonNull(view);
        shown = view;
        content.getChildren().clear();
        Label title = new Label(view.title());
        title.setWrapText(true);
        title.getStyleClass().add(TITLE_STYLE_CLASS);
        content.getChildren().add(title);
        for (DetailView.Section section : view.sections()) {
            if (!section.heading().isEmpty()) {
                Label heading = new Label(section.heading());
                heading.setWrapText(true);
                heading.getStyleClass().add(HEADING_STYLE_CLASS);
                content.getChildren().add(heading);
            }
            section.lines().forEach(line -> content.getChildren().add(toNode(line)));
        }
    }

    private static TextFlow toNode(DetailView.Line line) {
        Text text = new Text(line.text());
        text.getStyleClass().add(LINE_STYLE_CLASS);
        if (line.isWarning()) {
            text.getStyleClass().add(WARNING_STYLE_CLASS);
        }
        if (line.isStruckThrough()) {
            text.getStyleClass().add(STRUCK_STYLE_CLASS);
            text.setStrikethrough(true);
        }
        return new TextFlow(text);
    }
}
