package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class UserGuideTest {
    @Test
    public void loadHtml_missingGuide_throwsIoException() {
        assertThrows(IOException.class, "Missing resource: /missing-guide.md", () -> {
            UserGuide.loadHtml("/missing-guide.md", "/view/UserGuide.css");
        });
    }

    @Test
    public void loadHtml_missingStylesheet_throwsIoException() {
        assertThrows(IOException.class, "Missing resource: /missing-style.css", () -> {
            UserGuide.loadHtml("/guide/UserGuide.md", "/missing-style.css");
        });
    }

    @Test
    public void loadHtml_unbundledImage_preservesDestinationAndAltText() throws Exception {
        String html = UserGuide.loadHtml("/view/UserGuideTest/unbundled-image.md", "/view/UserGuide.css");
        assertTrue(html.contains("src=\"images/missing.png\" alt=\"Missing screenshot\""));
    }

    @Test
    public void loadHtml_currentGuide_rendersOfflineContent() throws Exception {
        String html = UserGuide.loadHtml();
        assertTrue(html.contains("<h1 id=\"tutorflow-user-guide\">TutorFlow User Guide</h1>"));
        assertTrue(html.contains("<table>"));
        assertTrue(html.contains("id=\"adding-a-student-add\""));
        assertTrue(html.contains("href=\"#adding-a-student-add\""));
        assertTrue(html.contains("<div class=\"callout\">"));
        assertFalse(html.contains("<page-nav-print"));
        assertFalse(html.contains("<box"));
        assertTrue(html.contains(UserGuide.class.getResource("/guide/images/Ui.png").toExternalForm()));
        assertNotNull(UserGuide.class.getResource("/guide/images/findAlexDavidResult.png"));
    }

    @Test
    public void bundledGuide_matchesCurrentDocumentation() throws Exception {
        try (var stream = UserGuide.class.getResourceAsStream("/guide/UserGuide.md")) {
            assertNotNull(stream);
            assertArrayEquals(Files.readAllBytes(Path.of("docs", "UserGuide.md")), stream.readAllBytes());
        }
    }
}
