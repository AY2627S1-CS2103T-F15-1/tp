import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javax.imageio.ImageIO;
import javafx.animation.AnimationTimer;
import javafx.application.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.stage.*;

/** External, visible-window GUI integration test of the unmodified released JAR. */
public class GuiJourney {
    static int checks, commands;
    static int resultCode = 1;
    static Path output;
    static String phase;
    static final StringBuilder transcript = new StringBuilder();
    static final List<Throwable> unexpected = new CopyOnWriteArrayList<>();
    static Stage stage;
    static TextField input;
    static TextArea result;

    public static void main(String[] args) throws Exception {
        phase = args.length == 0 ? "exercise" : args[0];
        output = Path.of("evidence-" + phase).toAbsolutePath();
        Files.createDirectories(output);
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            unexpected.add(e);
            e.printStackTrace();
        });
        Application.launch(App.class);
        System.exit(resultCode);
    }

    public static class App extends seedu.address.MainApp {
        @Override public void start(Stage primary) {
            super.start(primary);
            Platform.setImplicitExit(false);
            stage = primary;
            new Thread(() -> {
                try {
                    pulse();
                    fx(() -> {
                        input = (TextField) stage.getScene().lookup("#commandTextField");
                        result = (TextArea) stage.getScene().lookup("#resultDisplay");
                        return null;
                    });
                    check(fx(() -> stage.isShowing() && input != null && result != null),
                            "real MainApp window and FXML controls are showing");
                    log("Method: inputs entered into the actual TextField, submitted through synthetic Enter key events; "
                            + "actual result/card nodes inspected after render pulses. No direct LogicManager calls. "
                            + "Automated GUI integration testing, not human keyboard/mouse testing.\n");
                    if (phase.equals("exercise")) exercise();
                    else if (phase.equals("controls")) controls();
                    else reopen();
                    check(unexpected.isEmpty(), "no uncaught application/UI exceptions");
                    log("PASS: " + commands + " GUI commands, " + checks + " checks; phase=" + phase);
                    resultCode = 0;
                } catch (Throwable e) {
                    log("FAIL: " + e);
                    e.printStackTrace();
                    try { snapshot("failure"); } catch (Throwable ignored) { }
                } finally {
                    try { Files.writeString(output.resolve("transcript.txt"), transcript); }
                    catch (Exception e) { e.printStackTrace(); }
                    Platform.exit();
                }
            }, "gui-journey-driver").start();
        }
    }

    static <T> T fx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }
    static void pulse() throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> new AnimationTimer() {
            int frames;
            public void handle(long now) { if (++frames == 3) { stop(); done.countDown(); } }
        }.start());
        if (!done.await(20, TimeUnit.SECONDS)) throw new AssertionError("GUI did not render");
    }
    static void log(String text) { transcript.append(text).append('\n'); System.out.println(text); }
    static void check(boolean okay, String label) {
        checks++;
        if (!okay) throw new AssertionError(label);
        log("PASS: " + label);
    }
    static byte[] saved() throws Exception {
        Path file = Path.of("data/tutorflow-v14.json");
        return Files.exists(file) ? Files.readAllBytes(file) : new byte[0];
    }
    static String command(String text, boolean error, String... expected) throws Exception {
        commands++;
        byte[] before = saved();
        long begin = System.nanoTime();
        fx(() -> {
            input.replaceText(0, input.getLength(), text);
            input.requestFocus();
            check(input.getText().equals(text), "command box retains entire input (" + text.length() + " chars)");
            Event.fireEvent(input, new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.ENTER,false,false,false,false));
            Event.fireEvent(input, new KeyEvent(KeyEvent.KEY_RELEASED,"","",KeyCode.ENTER,false,false,false,false));
            return null;
        });
        pulse();
        String feedback = fx(() -> result.getText());
        log("\n> " + (text.length() > 180 ? text.substring(0, 160) + "... [" + text.length() + " chars]" : text)
                + "\n" + feedback + "\nGUI elapsed ms: " + (System.nanoTime() - begin) / 1_000_000);
        check(fx(() -> input.getStyleClass().contains("error")) == error, "command error styling matches outcome");
        check(fx(() -> result.getStyleClass().contains("error")) == error, "feedback error styling matches outcome");
        check(fx(() -> input.getText().equals(error ? text : "")), "failed input retained / successful input cleared");
        for (String part : expected) check(feedback.contains(part), "visible result contains " + part);
        if (error) check(Arrays.equals(before, saved()), "rejected input leaves saved data unchanged");
        check(fx(() -> stage.isShowing()), "main window remains open");
        return feedback;
    }
    static String cmd(String text, String... expected) throws Exception { return command(text, false, expected); }
    static void reject(String text, String... expected) throws Exception { command(text, true, expected); }
    static void snapshot(String name) throws Exception {
        pulse();
        WritableImage image = fx(() -> stage.getScene().snapshot(null));
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output.resolve(name + ".png").toFile());
        log("Screenshot: " + name + ".png (actual JavaFX scene)");
    }
    static String labels() throws Exception {
        return fx(() -> stage.getScene().getRoot().lookupAll(".label").stream()
                .filter(n -> n instanceof Label && n.isVisible())
                .map(n -> ((Label) n).getText()).reduce("", (a,b) -> a + "\n" + b));
    }
    static void fireButton(String selector) throws Exception {
        fx(() -> {
            Button button = (Button) stage.getScene().lookup(selector);
            check(button != null && !button.isDisabled(), "button exists and is enabled: " + selector);
            button.fire();
            return null;
        });
        pulse();
    }
    static void exercise() throws Exception {
        snapshot("01-empty-launch");
        fireButton("#addStudentButton");
        check(fx(() -> input.getText().equals("add n/")), "plus button prepares add command without creating data");
        fireButton("#clearCommandButton");
        check(fx(() -> input.getText().isEmpty()), "clear control empties command box");
        cmd("add n/Alice p/91234567 l/S3 s/Math r/60 e/alice@example.com v/Bishan", "New student added", "Alice");
        check(labels().contains("alice@example.com") && labels().contains("Bishan"), "student card renders contact fields");
        snapshot("02-student-card");
        cmd("lesson add st/1 d/today t/08:00 dur/90", "Scheduled:");
        cmd("lesson add st/1 d/today t/10:00", "Scheduled:");
        cmd("lesson list st/1", "08:00", "10:00", "1.", "2.");
        cmd("lesson done 1 n/Covered   algebra. x² = 4.", "Completed:", "$90.00", "Lesson numbers have changed", "10:00");
        snapshot("03-complete-renumber");
        cmd("lesson missed 1", "Marked missed:", "10:00");
        cmd("lesson list st/1 all/", "08:00", "completed", "10:00", "missed");
        cmd("view 1", "Covered algebra. x² = 4.", "$90.00");
        snapshot("04-completed-and-missed-overview");
        cmd("lesson done 1 n/Revised teaching notes", "Updated notes");
        cmd("lesson done 1", "already completed", "retained");
        cmd("balance st/1", "$90.00");
        reject("lesson missed 1", "completed");
        reject("lesson done 0", "positive integer");
        reject("lesson done 999999999999999999999999999999999999", "positive integer");
        reject("lesson done 1 n/", "Empty value");
        reject("lesson done 1 n/One n/Two", "Multiple values");
        snapshot("05-validation-error");
        cmd("lesson missed 2", "already marked as missed");
        cmd("lesson done 2 n/Actually taught", "Completed:", "$60.00");
        cmd("lesson done 2", "already completed");
        cmd("balance st/1", "$150.00");
        cmd("lesson add st/1 d/tmr t/08:00", "Scheduled:");
        cmd("lesson list st/1 all/", "3.");
        reject("lesson done 3", "on or after");
        reject("lesson missed 3", "on or after");
        cmd("lesson cancel 3 r/Student unwell", "Cancelled:");
        reject("lesson done 3", "cancelled");
        cmd("edit 1 e/", "Edited");
        check(!labels().contains("alice@example.com") && labels().contains("Bishan"), "email disappears from card while venue stays");
        cmd("edit 1 v/", "Edited");
        check(!labels().contains("Bishan"), "cleared current venue disappears from student card");
        cmd("lesson list st/1 all/", "Bishan");
        cmd("hw add st/1 desc/Algebra worksheet due/tmr", "Algebra worksheet");
        cmd("hw list", "Algebra worksheet");
        reject("hw feedback 1 f/Too early", "submitted");
        cmd("hw status 1 s/submitted", "submitted");
        cmd("hw feedback 1 f/Please revise question two", "Feedback recorded");
        cmd("hw feedback 1 f/", "Feedback cleared");
        cmd("hw list", "Algebra worksheet", "submitted");
        cmd("add n/Işık p/92345678 l/S3 s/Math r/50", "New student added");
        cmd("find ışık", "1 student");
        check(labels().contains("Işık") && !labels().contains("Alice"), "Unicode search visibly filters and renumbers cards");
        snapshot("06-unicode-filter");
        cmd("list", "Listed active students.");
        check(fx(() -> ((ListView<?>) stage.getScene().lookup("#personListView")).getItems().size() == 2), "list displays two student cards");
        cmd("lesson list st/1 all/");
        cmd("lesson done 1 n/" + "a".repeat(1000), "Updated notes");
        reject("lesson done 1 n/" + "b".repeat(1001), "1-1000");
        reject("lesson done 1 n/" + "b".repeat(200000), "1-1000");
        cmd("lesson done 1 n/Final persisted teaching notes", "Updated notes");
        fx(() -> { input.replaceText(0, input.getLength(), "draft to preserve");
            Event.fireEvent(input, new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.UP,false,false,false,false)); return null; });
        check(fx(() -> input.getText().equals("lesson done 1 n/Final persisted teaching notes")), "Up recalls last command through key event");
        fx(() -> { Event.fireEvent(input, new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.DOWN,false,false,false,false)); return null; });
        check(fx(() -> input.getText().equals("draft to preserve")), "Down restores unfinished draft");
        fireButton("#clearCommandButton");
        cmd("view 1", "Final persisted teaching notes", "Actually taught", "$150.00");
        snapshot("07-final-overview");
        fx(() -> { result.setScrollTop(Double.MAX_VALUE); return null; });
        snapshot("07b-final-overview-scrolled");
        fx(() -> { stage.setWidth(450); stage.setHeight(600); return null; });
        pulse();
        check(fx(() -> input.getWidth() > 100 && result.getWidth() > 100 && input.isVisible()), "command and result controls remain usable at minimum window size");
        snapshot("08-minimum-window");
        fx(() -> { stage.setWidth(900); stage.setHeight(780); return null; });
        pulse();
        cmd("balance st/1", "$150.00");
        fx(() -> { Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)); return null; });
        check(fx(() -> !stage.isShowing()), "window close request closes through normal application handler");
    }
    static void reopen() throws Exception {
        cmd("list", "Listed active students.");
        check(fx(() -> ((ListView<?>) stage.getScene().lookup("#personListView")).getItems().size() == 2), "list displays two student cards");
        check(labels().contains("Alice") && labels().contains("Işık"), "student cards restore after process restart");
        check(!labels().contains("alice@example.com") && !labels().contains("Bishan"), "cleared current fields stay cleared after restart");
        cmd("lesson list st/1 all/", "completed", "cancelled", "Bishan", "3 lesson");
        cmd("view 1", "Final persisted teaching notes", "Actually taught", "$150.00");
        snapshot("01-restored-overview");
        cmd("hw list", "Algebra worksheet", "submitted");
        check(!fx(() -> result.getText()).contains("Please revise question two"), "cleared feedback remains absent after restart");
        cmd("find ışık", "1 student");
        check(labels().contains("Işık") && !labels().contains("Alice"), "Unicode search still filters after restart");
        snapshot("02-restored-unicode-search");
        cmd("list", "Listed active students.");
        check(fx(() -> ((ListView<?>) stage.getScene().lookup("#personListView")).getItems().size() == 2), "list displays two student cards");
        fx(() -> { input.replaceText(0, input.getLength(), "exit"); Event.fireEvent(input, new ActionEvent(input,input)); return null; });
        pulse();
        check(fx(() -> !stage.isShowing()), "exit command closes the real window");
    }
    static void controls() throws Exception {
        cmd("list", "Listed active students.");
        Node card = fx(() -> stage.getScene().getRoot().lookupAll("#cardPane").stream()
                .filter(n -> ((Label) n.lookup("#name")).getText().equals("Alice"))
                .findFirst().orElseThrow());
        byte[] before = saved();
        fx(() -> { card.requestFocus(); return null; });
        pulse();
        check(fx(() -> card.lookup("#cardEditButton").isVisible()), "keyboard focus reveals card actions");
        fx(() -> { ((Button) card.lookup("#cardEditButton")).fire(); return null; });
        pulse();
        check(fx(() -> card.lookup("#emailEditButton").isVisible()), "edit action reveals field pencils");
        fx(() -> { ((Button) card.lookup("#emailEditButton")).fire(); return null; });
        check(fx(() -> input.getText().equals("edit 1 e/")), "email pencil prepares correct indexed command");
        snapshot("01-card-edit-draft");
        fx(() -> { ((Button) card.lookup("#cancelEditButton")).fire(); return null; });
        check(fx(() -> input.getText().isEmpty() && !card.lookup("#emailEditButton").isVisible()),
                "Cancel clears unchanged generated draft and hides field pencils");
        fx(() -> { card.requestFocus(); ((Button) card.lookup("#cardEditButton")).fire();
            ((Button) card.lookup("#phoneEditButton")).fire();
            input.appendText("98765432");
            ((Button) card.lookup("#cancelEditButton")).fire(); return null; });
        check(fx(() -> input.getText().equals("edit 1 p/98765432")), "Cancel preserves draft changed by user");
        fireButton("#clearCommandButton");
        fx(() -> { card.requestFocus(); ((Button) card.lookup("#cardDeleteButton")).fire(); return null; });
        check(fx(() -> input.getText().equals("delete 1")), "delete shortcut prepares command instead of deleting immediately");
        check(Arrays.equals(before, saved()), "card shortcut interactions do not change saved records");
        fireButton("#clearCommandButton");
        fx(() -> { input.requestFocus(); Event.fireEvent(input,
            new KeyEvent(KeyEvent.KEY_PRESSED,"","",KeyCode.F1,false,false,false,false)); return null; });
        pulse();
        Stage help = fx(() -> Window.getWindows().stream().filter(w -> w != stage && w.isShowing() && w instanceof Stage)
                .map(w -> (Stage) w).findFirst().orElseThrow());
        check(fx(() -> help.getScene() != null && help.isShowing()), "F1 opens the offline help window from command input");
        WritableImage image = fx(() -> help.getScene().snapshot(null));
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output.resolve("02-offline-help.png").toFile());
        log("Screenshot: 02-offline-help.png (actual help scene)");
        fx(() -> { help.hide(); return null; });
        cmd("help lesson done", "lesson done");
        check(Arrays.equals(before, saved()), "help and shortcut interactions preserve all saved records");
        fx(() -> { Event.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)); return null; });
        check(fx(() -> !stage.isShowing()), "window closes after shortcut/help testing");
    }
}
