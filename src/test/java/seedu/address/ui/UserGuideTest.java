package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    }

    @Test
    public void loadHtml_currentGuide_linksToCurrentLessonSectionsInContents() throws Exception {
        String html = UserGuide.loadHtml();

        assertTrue(html.contains("<nav aria-label=\"Contents\">"));
        String contents = html.substring(html.indexOf("<nav"), html.indexOf("</nav>"));
        assertTrue(contents.contains("href=\"#scheduling-a-lesson-lesson-add\""));
        assertTrue(contents.contains("href=\"#rescheduling-a-lesson-lesson-move\""));
        assertTrue(contents.contains("href=\"#completing-a-lesson-and-recording-notes-lesson-done\""));
        assertTrue(contents.contains("href=\"#recording-a-missed-lesson-lesson-missed\""));
    }

    @Test
    public void loadHtml_currentGuide_everyLinkWithinTheGuideHasATarget() throws Exception {
        String html = UserGuide.loadHtml();

        Matcher links = Pattern.compile("href=\"#([^\"]+)\"").matcher(html);
        int count = 0;
        while (links.find()) {
            count++;
            assertTrue(html.contains("id=\"" + links.group(1) + "\""), "No section has the id " + links.group(1));
        }
        assertTrue(count > 20, "the links of the guide were not read");
    }

    @Test
    public void loadHtml_currentGuide_noTwoSectionsShareAnId() throws Exception {
        String html = UserGuide.loadHtml();

        Matcher ids = Pattern.compile("<h[1-6] id=\"([^\"]+)\"").matcher(html);
        Set<String> seen = new HashSet<>();
        while (ids.find()) {
            assertTrue(seen.add(ids.group(1)), "Two sections have the id " + ids.group(1));
        }
    }

    @Test
    public void bundledGuide_matchesCurrentDocumentation() throws Exception {
        try (var stream = UserGuide.class.getResourceAsStream("/guide/UserGuide.md")) {
            assertNotNull(stream);
            assertArrayEquals(Files.readAllBytes(Path.of("docs", "UserGuide.md")), stream.readAllBytes());
        }
    }
}
