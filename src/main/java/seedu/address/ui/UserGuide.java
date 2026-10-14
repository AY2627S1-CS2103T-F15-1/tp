package seedu.address.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
    private UserGuide() {}

    /**
     * Reads the guide and its stylesheet from application resources, without a network connection.
     *
     * @throws IOException if a required resource cannot be read
     */
    public static String loadHtml() throws IOException {
        String markdown = readResource("/guide/UserGuide.md")
                .replace("<page-nav-print />", "")
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
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<title>TutorFlow User Guide</title><style>"
                + readResource("/view/UserGuide.css") + "</style></head><body>" + body + "</body></html>";
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
