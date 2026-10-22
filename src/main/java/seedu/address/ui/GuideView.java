package seedu.address.ui;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Displays the bundled guide with JavaFX controls and the JDK's HTML parser.
 * No browser engine, scripts, external stylesheets or network resources are loaded.
 */
public class GuideView extends ScrollPane {
    private final VBox document = new VBox(10);
    private final Map<String, Node> anchors = new HashMap<>();
    private Consumer<String> statusHandler = message -> { };

    /** Creates a wrapping, scrollable offline document. */
    public GuideView() {
        setFitToWidth(true);
        document.setPadding(new Insets(24));
        document.setStyle("-fx-background-color: white;");
        setContent(document);
    }

    public void setStatusHandler(Consumer<String> handler) {
        statusHandler = handler;
    }

    /** Replaces the document with locally rendered HTML. Must run on the JavaFX thread. */
    public void loadHtml(String html) throws IOException {
        document.getChildren().clear();
        anchors.clear();
        new ParserDelegator().parse(new StringReader(html), new Renderer(), true);
        setVvalue(0);
    }

    /** Follows only a local contents link; all other destinations stay offline. */
    void navigate(String destination) {
        if (!destination.startsWith("#")) {
            statusHandler.accept("Help works offline. External navigation is disabled.");
            return;
        }
        Node target = anchors.get(destination.substring(1));
        if (target != null) {
            document.applyCss();
            document.layout();
            double y = document.sceneToLocal(target.localToScene(0, 0)).getY();
            double scrollableHeight = document.getHeight() - getViewportBounds().getHeight();
            setVvalue(scrollableHeight <= 0 ? 0 : Math.min(1, y / scrollableHeight));
            statusHandler.accept("");
        }
    }

    /** Translates the guide's headings, paragraphs, lists, tables and inline formatting into controls. */
    private final class Renderer extends HTMLEditorKit.ParserCallback {
        private TextFlow flow;
        private GridPane table;
        private int row;
        private int column;
        private int heading;
        private int bold;
        private int italic;
        private int code;
        private int hidden;
        private String link;
        private final Deque<Integer> listCounters = new ArrayDeque<>();

        private TextFlow currentFlow() {
            if (flow == null) {
                flow = new TextFlow();
                flow.setLineSpacing(4);
                if (table == null) {
                    document.getChildren().add(flow);
                } else {
                    table.add(flow, column++, row);
                    GridPane.setHgrow(flow, Priority.ALWAYS);
                    flow.setPadding(new Insets(8));
                    flow.setStyle("-fx-border-color: #d0d7de;");
                }
            }
            return flow;
        }

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
            String name = tag.toString();
            if (name.equals("head") || name.equals("script") || name.equals("style")) {
                hidden++;
                return;
            }
            if (hidden > 0) {
                return;
            }
            switch (name) {
                case "ol", "ul":
                    listCounters.push(name.equals("ol") ? 1 : 0);
                    break;
                case "h1", "h2", "h3", "h4", "h5", "h6":
                    flow = null;
                    heading = Integer.parseInt(name.substring(1));
                    break;
                case "p", "div", "pre", "li":
                    if (table == null) {
                        flow = null;
                    }
                    if (name.equals("pre")) {
                        code++;
                    }
                    if (name.equals("li")) {
                        int number = listCounters.isEmpty() ? 0 : listCounters.peek();
                        append(number == 0 ? "• " : number + ". ");
                        if (number > 0) {
                            listCounters.pop();
                            listCounters.push(number + 1);
                        }
                    }
                    break;
                case "b", "strong", "th":
                    bold++;
                    if (name.equals("th")) {
                        flow = null;
                    }
                    break;
                case "i", "em":
                    italic++;
                    break;
                case "code":
                    code++;
                    break;
                case "a":
                    Object destination = attributes.getAttribute(HTML.Attribute.HREF);
                    link = destination == null ? null : destination.toString();
                    break;
                case "table":
                    flow = null;
                    table = new GridPane();
                    table.setMaxWidth(Double.MAX_VALUE);
                    table.prefWidthProperty().bind(document.widthProperty().subtract(48));
                    document.getChildren().add(table);
                    row = -1;
                    break;
                case "tr":
                    row++;
                    column = 0;
                    flow = null;
                    break;
                case "td":
                    flow = null;
                    break;
                default:
                    break;
            }
            Object id = attributes.getAttribute(HTML.Attribute.ID);
            if (id != null) {
                anchors.put(id.toString(), currentFlow());
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int position) {
            String name = tag.toString();
            if (name.equals("head") || name.equals("script") || name.equals("style")) {
                hidden--;
                return;
            }
            if (hidden > 0) {
                return;
            }
            switch (name) {
                case "h1", "h2", "h3", "h4", "h5", "h6":
                    heading = 0;
                    flow = null;
                    break;
                case "b", "strong", "th":
                    bold--;
                    break;
                case "i", "em":
                    italic--;
                    break;
                case "code", "pre":
                    code--;
                    break;
                case "a":
                    link = null;
                    break;
                case "table":
                    int columns = table.getColumnCount();
                    for (int index = 0; index < columns; index++) {
                        ColumnConstraints constraints = new ColumnConstraints();
                        constraints.setPercentWidth(100.0 / columns);
                        table.getColumnConstraints().add(constraints);
                    }
                    table = null;
                    flow = null;
                    break;
                case "ul", "ol":
                    if (!listCounters.isEmpty()) {
                        listCounters.pop();
                    }
                    flow = null;
                    break;
                case "p", "div", "li":
                    if (table == null) {
                        flow = null;
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void handleText(char[] text, int position) {
            if (hidden == 0) {
                append(new String(text));
            }
        }

        private void append(String value) {
            if (link != null) {
                String destination = link;
                Hyperlink hyperlink = new Hyperlink(value);
                hyperlink.setWrapText(true);
                hyperlink.setOnAction(event -> navigate(destination));
                currentFlow().getChildren().add(hyperlink);
            } else {
                Text text = new Text(value);
                text.setFont(Font.font(code > 0 ? "Monospaced" : "System",
                        bold > 0 || heading > 0 ? FontWeight.BOLD : FontWeight.NORMAL,
                        italic > 0 ? FontPosture.ITALIC : FontPosture.REGULAR,
                        heading > 0 ? Math.max(16, 30 - heading * 3) : 14));
                currentFlow().getChildren().add(text);
            }
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
            if (hidden > 0) {
                return;
            }
            if (tag == HTML.Tag.BR) {
                append("\n");
            } else if (tag == HTML.Tag.IMG) {
                String source = String.valueOf(attributes.getAttribute(HTML.Attribute.SRC));
                var bundledImage = UserGuide.class.getResource("/guide/images/Ui.png");
                // Only the bundled screenshot is used by the guide. Never fetch arbitrary image URLs.
                if (bundledImage != null && source.equals(bundledImage.toExternalForm())) {
                    ImageView image = new ImageView(new Image(source));
                    image.setPreserveRatio(true);
                    image.fitWidthProperty().bind(widthProperty().subtract(60));
                    currentFlow().getChildren().add(image);
                } else {
                    Object alt = attributes.getAttribute(HTML.Attribute.ALT);
                    append(alt == null ? "[Image unavailable]" : alt.toString());
                }
            }
        }
    }
}
