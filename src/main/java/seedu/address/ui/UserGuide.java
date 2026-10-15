package seedu.address.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Renders the project's bundled Markdown User Guide for the help window.
 */
public final class UserGuide {
    private static final String CONTENTS_PLACEHOLDER = "<!-- user-guide-contents -->";
    private static final Pattern SECTION_HEADING = Pattern.compile("<h([23]) id=\"([^\"]+)\">(.*?)</h\\1>",
            Pattern.DOTALL);

    private UserGuide() {}

    /**
     * Reads the guide and its stylesheet from application resources, without a network connection.
     *
     * @throws IOException if a required resource cannot be read
     */
    public static String loadHtml() throws IOException {
        return loadHtml("/guide/UserGuide.md", "/view/UserGuide.css");
    }

    /**
     * Renders the guide at the given resource paths.
     */
    static String loadHtml(String guidePath, String stylesheetPath) throws IOException {
        String markdown = readResource(guidePath)
                .replace("<page-nav-print />", CONTENTS_PLACEHOLDER)
                .replaceAll("<box[^>]*>", "<div class=\"callout\">")
                .replace("</box>", "</div>");
        List<Extension> extensions = List.of(TablesExtension.create(), HeadingAnchorExtension.create());
        Node document = Parser.builder().extensions(extensions).build().parse(markdown);
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Image image) {
                URL resource = UserGuide.class.getResource("/guide/" + image.getDestination());
                if (resource != null) {
                    image.setDestination(resource.toExternalForm());
                }
                visitChildren(image);
            }
        });
        String body = HtmlRenderer.builder().extensions(extensions).build().render(document);
        body = body.replace(CONTENTS_PLACEHOLDER, formatContents(body));
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<title>TutorFlow User Guide</title><style>"
                + readResource(stylesheetPath) + "</style></head><body>" + body + "</body></html>";
    }

    /**
     * Replaces the website's navigation component with links to the anchors produced by the Markdown renderer.
     */
    private static String formatContents(String body) {
        Matcher headings = SECTION_HEADING.matcher(body);
        StringBuilder contents = new StringBuilder("<nav aria-label=\"Contents\"><h2>Contents</h2><ul>");
        while (headings.find()) {
            contents.append("<li><a href=\"#").append(headings.group(2)).append("\">")
                    .append(headings.group(3)).append("</a></li>");
        }
        return contents.append("</ul></nav>").toString();
    }

    private static String readResource(String path) throws IOException {
        try (InputStream stream = UserGuide.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Missing resource: " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
