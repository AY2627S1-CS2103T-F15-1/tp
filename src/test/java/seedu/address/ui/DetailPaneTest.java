package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.ui.FxTestSupport.onFx;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.view.DetailView;

/** Tests the pane that draws a {@link DetailView}, on the JavaFX application thread. */
public class DetailPaneTest {
    private Stage stage;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        FxTestSupport.start();
    }

    @AfterEach
    public void closeWindow() throws Exception {
        onFx(() -> {
            if (stage != null) {
                stage.hide();
            }
            return null;
        });
    }

    private DetailPane show(double width, double height) throws Exception {
        return onFx(() -> {
            DetailPane pane = new DetailPane();
            stage = new Stage();
            stage.setScene(new Scene(pane.getRoot(), width, height));
            stage.show();
            return pane;
        });
    }

    private static void layout(DetailPane pane) {
        pane.getRoot().getScene().getRoot().applyCss();
        pane.getRoot().getScene().getRoot().layout();
    }

    private static List<Text> texts(DetailPane pane) {
        List<Text> texts = new ArrayList<>();
        for (Node node : pane.getRoot().lookupAll("." + DetailPane.LINE_STYLE_CLASS)) {
            texts.add((Text) node);
        }
        return texts;
    }

    private static DetailView manyLines(int count) {
        List<DetailView.Line> lines = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lines.add(DetailView.Line.plain("Line " + i));
        }
        return new DetailView("Many", "Many", false, List.of(new DetailView.Section("Section", lines)));
    }

    @Test
    public void show_drawsTheTitleHeadingsAndLinesInOrder() throws Exception {
        DetailPane pane = show(400, 300);
        DetailView view = new DetailView("Title", "Summary", false, List.of(
                new DetailView.Section("First", List.of(DetailView.Line.plain("a"), DetailView.Line.plain("b"))),
                new DetailView.Section("", List.of(DetailView.Line.plain("c"))),
                new DetailView.Section("Second", List.of())));

        onFx(() -> {
            pane.show(view);
            layout(pane);
            return null;
        });

        assertEquals(List.of("Title"), pane.getRoot().lookupAll("." + DetailPane.TITLE_STYLE_CLASS).stream()
                .map(node -> ((Label) node).getText()).toList());
        assertEquals(List.of("First", "Second"), pane.getRoot().lookupAll("." + DetailPane.HEADING_STYLE_CLASS)
                .stream().map(node -> ((Label) node).getText()).toList());
        assertEquals(List.of("a", "b", "c"), texts(pane).stream().map(Text::getText).toList());
        assertEquals(view, pane.getShown().orElseThrow());
    }

    @Test
    public void show_cancelledAndWarningLines_areDrawnFromTheirFlagsNotTheirText() throws Exception {
        DetailPane pane = show(400, 300);
        DetailView view = new DetailView("T", "S", false, List.of(new DetailView.Section("H", List.of(
                new DetailView.Line("plain line", false, false),
                new DetailView.Line("struck line", true, false),
                new DetailView.Line("warning line", false, true),
                // text that says cancelled, which must not be struck unless the line says so
                new DetailView.Line("1. 10:00 Amy Math (cancelled)", false, false)))));

        onFx(() -> {
            pane.show(view);
            layout(pane);
            return null;
        });

        List<Text> texts = texts(pane);
        assertFalse(texts.get(0).isStrikethrough());
        assertTrue(texts.get(1).isStrikethrough());
        assertTrue(texts.get(1).getStyleClass().contains(DetailPane.STRUCK_STYLE_CLASS));
        assertFalse(texts.get(2).isStrikethrough());
        assertTrue(texts.get(2).getStyleClass().contains(DetailPane.WARNING_STYLE_CLASS));
        assertFalse(texts.get(3).isStrikethrough());
        assertFalse(texts.get(3).getStyleClass().contains(DetailPane.STRUCK_STYLE_CLASS));
    }

    @Test
    public void show_again_replacesWhatWasShown() throws Exception {
        DetailPane pane = show(400, 300);

        onFx(() -> {
            pane.show(manyLines(5));
            pane.show(new DetailView("Next", "Next", false, List.of()));
            layout(pane);
            return null;
        });

        assertTrue(texts(pane).isEmpty());
        assertEquals("Next", pane.getShown().orElseThrow().title());
    }

    @Test
    public void show_startsAtTheTop_andRefreshStaysWhereTheUserHasScrolled() throws Exception {
        DetailPane pane = show(400, 200);
        ScrollPane scroll = (ScrollPane) pane.getRoot();

        double refreshed = onFx(() -> {
            pane.show(manyLines(100));
            layout(pane);
            scroll.setVvalue(0.5);
            pane.refresh(manyLines(100));
            return scroll.getVvalue();
        });
        double reshown = onFx(() -> {
            pane.show(manyLines(100));
            return scroll.getVvalue();
        });

        assertEquals(0.5, refreshed, 0.001);
        assertEquals(0, reshown, 0.001);
    }

    @Test
    public void scrollPage_movesByMostOfAScreenAndStopsAtTheEnds() throws Exception {
        DetailPane pane = show(400, 200);
        ScrollPane scroll = (ScrollPane) pane.getRoot();

        double[] positions = onFx(() -> {
            pane.show(manyLines(100));
            layout(pane);
            assertTrue(scroll.getViewportBounds().getHeight() > 0, "The pane must have been laid out");
            pane.scrollPage(false);
            double atTop = scroll.getVvalue();
            pane.scrollPage(true);
            double afterPage = scroll.getVvalue();
            for (int i = 0; i < 200; i++) {
                pane.scrollPage(true);
            }
            double atBottom = scroll.getVvalue();
            pane.scrollPage(false);
            return new double[] {atTop, afterPage, atBottom, scroll.getVvalue()};
        });

        assertEquals(0, positions[0], 0.0001);
        double pageFraction = positions[1];
        assertTrue(pageFraction > 0 && pageFraction < 0.5, "a page is most of a screen: " + pageFraction);
        assertEquals(1, positions[2], 0.0001);
        assertTrue(positions[3] < 1);
    }

    @Test
    public void scrollLines_movesByAFewLinesWhichIsLessThanAPage() throws Exception {
        DetailPane pane = show(400, 200);
        ScrollPane scroll = (ScrollPane) pane.getRoot();

        double[] moves = onFx(() -> {
            pane.show(manyLines(100));
            layout(pane);
            pane.scrollLines(true);
            double lines = scroll.getVvalue();
            scroll.setVvalue(0);
            pane.scrollPage(true);
            double page = scroll.getVvalue();
            pane.scrollLines(false);
            return new double[] {lines, page, scroll.getVvalue()};
        });

        assertTrue(moves[0] > 0, "ctrl+down scrolls");
        assertTrue(moves[0] < moves[1], "a few lines are less than a page");
        assertTrue(moves[2] < moves[1], "ctrl+up scrolls back");
    }

    @Test
    public void scrolling_contentShorterThanThePane_doesNothing() throws Exception {
        DetailPane pane = show(400, 400);
        ScrollPane scroll = (ScrollPane) pane.getRoot();

        double position = onFx(() -> {
            pane.show(manyLines(2));
            layout(pane);
            pane.scrollPage(true);
            pane.scrollLines(true);
            return scroll.getVvalue();
        });

        assertEquals(0, position, 0.0001);
    }
}
