/*
 * Unmaintained snapshot for the published TutorFlow v1.5 JAR (1 November 2026).
 * Outside Gradle/CI/Checkstyle; later app internals may not be compatible.
 * See README.md for prerequisites, isolation and reproduction instructions.
 */
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Runs isolated command journeys against the actual classes inside an unmodified TutorFlow JAR. */
public class FinalLessonCheck {
    static Path root;
    static PrintWriter log;
    static int checks;
    static int commands;

    static void check(boolean condition, String name) {
        checks++;
        if (!condition) {
            throw new AssertionError(name);
        }
        log.println("PASS: " + name);
        log.flush();
    }

    static class Session {
        final StorageManager storage;
        ModelManager model;
        LogicManager logic;

        Session(String name, String rate) throws Exception {
            Path dir = root.resolve(name);
            Files.createDirectories(dir);
            storage = new StorageManager(new JsonAddressBookStorage(dir.resolve("tutorflow-v14.json")),
                    new JsonUserPrefsStorage(dir.resolve("preferences.json")));
            model = new ModelManager(new AddressBook(), new UserPrefs());
            logic = new LogicManager(model, storage);
            ok("add n/Alice p/91234567 l/S3 s/Math r/" + rate);
        }

        String snapshot() throws Exception {
            return storage.snapshotAddressBook(model.getAddressBook());
        }

        String file() throws Exception {
            return Files.exists(storage.getAddressBookFilePath())
                    ? Files.readString(storage.getAddressBookFilePath()) : "<absent>";
        }

        String ok(String command) throws Exception {
            commands++;
            log.println("\n> " + abbreviated(command));
            long start = System.nanoTime();
            String result = logic.execute(command).getFeedbackToUser();
            log.println(abbreviated(result));
            log.printf("elapsed: %.3f ms%n", (System.nanoTime() - start) / 1e6);
            check(!logic.hasUnsavedChanges(), "successful command has no pending save");
            check(snapshot().equals(storage.snapshotAddressBook(storage.readAddressBook().orElseThrow())),
                    "persisted snapshot matches live data");
            return result;
        }

        void reject(String command) throws Exception {
            commands++;
            String before = snapshot();
            String diskBefore = file();
            log.println("\n> REJECT " + abbreviated(command));
            long start = System.nanoTime();
            try {
                logic.execute(command);
                throw new AssertionError("Unexpected acceptance: " + abbreviated(command));
            } catch (CommandException | ParseException expected) {
                log.println(expected.getClass().getSimpleName() + ": " + expected.getMessage());
            }
            log.printf("elapsed: %.3f ms%n", (System.nanoTime() - start) / 1e6);
            check(before.equals(snapshot()), "rejected command preserves in-memory records");
            check(diskBefore.equals(file()), "rejected command preserves saved bytes");
        }

        void reopen() throws Exception {
            String before = snapshot();
            model = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
            logic = new LogicManager(model, storage);
            check(before.equals(snapshot()), "new model and logic recover the saved state");
        }

        Lesson lesson(int zeroBased) {
            return model.getAddressBook().getLessonList().get(zeroBased);
        }

        void select() throws Exception {
            ok("lesson list st/1 all/");
        }

        void amount(String expected, int entries) {
            BigDecimal sum = model.getAddressBook().getLedgerEntries().stream()
                    .map(e -> e.amount()).reduce(BigDecimal.ZERO, BigDecimal::add);
            check(sum.compareTo(new BigDecimal(expected)) == 0
                            && model.getAddressBook().getLedgerEntries().size() == entries,
                    "expected charge total " + expected + " and entry count " + entries);
        }
    }

    static String abbreviated(String text) {
        return text.length() < 1500 ? text : text.substring(0, 180)
                + " ... [total UTF-16 length=" + text.length() + "]";
    }

    static void completion() throws Exception {
        Session s = new Session("completion-notes-and-rejections", "60");
        s.ok("lesson add st/1 d/today t/08:00 dur/90");
        s.select();
        UUID id = s.lesson(0).getId();
        s.ok("lesson done 1 n/Covered   algebra. x² = 4.");
        check(s.lesson(0).getStatus() == LessonStatus.COMPLETED, "completion updates status");
        check(s.lesson(0).getNotes().orElseThrow().equals("Covered algebra. x² = 4."),
                "notes normalize repeated spaces and retain Unicode");
        s.amount("90.00", 1);
        s.ok("lesson done 1");
        s.amount("90.00", 1);
        s.ok("lesson done 1 n/Revised notes");
        s.ok("lesson done 1");
        check(s.lesson(0).getNotes().orElseThrow().equals("Revised notes"), "omitted notes retain revised notes");
        check(s.lesson(0).getId().equals(id), "revising notes retains lesson identity");
        s.ok("lesson done 1 n/" + "a".repeat(1000));
        check(s.lesson(0).getNotes().orElseThrow().length() == 1000, "1000-character notes accepted");
        s.reopen();
        check(s.lesson(0).getNotes().orElseThrow().length() == 1000, "1000-character notes survive reopening");
        s.select();
        s.reject("lesson done 1 n/" + "a".repeat(1001));
        s.reject("lesson done 1 n/" + "a".repeat(200000));
        s.reject("lesson done 1 n/");
        s.reject("lesson done 1 n/One n/Two");
        s.reject("lesson done 1 n/Line one\nLine two");
        s.reject("lesson done 1 n/Line\u2028separator");
        s.reject("lesson done 0");
        s.reject("lesson done -1");
        s.reject("lesson done 999999999999999999999999999999999999");
        s.reject("lesson done 99");
        s.reject("lesson done 1 notes/Unexpected");
        s.reject("lesson missed 1");
        s.ok("lesson add st/1 d/tmr t/08:00");
        s.select();
        s.reject("lesson done 2 n/Future");
        s.reject("lesson missed 2");
        s.ok("lesson cancel 2 r/Test cancellation");
        s.reject("lesson done 2");
        s.reject("lesson missed 2");
        s.amount("90.00", 1);
    }

    static void missed() throws Exception {
        Session s = new Session("missed-correction", "60");
        s.ok("lesson add st/1 d/today t/10:00");
        s.select();
        UUID id = s.lesson(0).getId();
        s.ok("lesson missed 1");
        check(s.lesson(0).getStatus() == LessonStatus.MISSED, "missed updates status");
        s.amount("0", 0);
        s.ok("lesson missed 1");
        s.amount("0", 0);
        s.reject("lesson missed 1 extra");
        s.reject("lesson missed 1 n/Unexpected");
        s.reject("lesson move 1 d/tmr");
        s.reopen();
        check(s.lesson(0).getStatus() == LessonStatus.MISSED, "missed status survives reopening");
        s.select();
        s.ok("lesson done 1 n/Actually taught");
        check(s.lesson(0).getStatus() == LessonStatus.COMPLETED && s.lesson(0).getId().equals(id),
                "missed-to-completed correction retains identity");
        s.amount("60.00", 1);
        s.ok("lesson done 1");
        s.amount("60.00", 1);
        s.reject("lesson missed 1");
    }

    static void charging() throws Exception {
        Session zero = new Session("zero-rate", "0");
        zero.ok("lesson add st/1 d/today t/09:00");
        zero.select();
        check(zero.ok("lesson done 1").contains("No automatic charge"), "zero-rate diagnostic");
        check(zero.lesson(0).getStatus() == LessonStatus.COMPLETED, "zero rate still completes lesson");
        zero.amount("0", 0);
        Session rounded = new Session("zero-after-rounding", "0.01");
        rounded.ok("lesson add st/1 d/today t/09:00 dur/15");
        rounded.select();
        rounded.ok("lesson done 1");
        rounded.amount("0", 0);
        Session charged = new Session("manual-charge-then-complete", "60");
        charged.ok("lesson add st/1 d/today t/09:00");
        charged.select();
        charged.ok("lesson missed 1");
        charged.ok("charge l/1 amt/25");
        charged.reject("lesson cancel 1");
        charged.ok("lesson done 1");
        charged.amount("25", 1);
        Session cancellation = new Session("cancel-uncharged-missed", "60");
        cancellation.ok("lesson add st/1 d/today t/09:00");
        cancellation.select();
        cancellation.ok("lesson missed 1");
        cancellation.ok("lesson cancel 1 r/Mistaken booking");
        check(cancellation.lesson(0).getStatus() == LessonStatus.CANCELLED,
                "uncharged missed lessons can be cancelled as currently documented");
        cancellation.amount("0", 0);
    }

    static void selection() throws Exception {
        Session s = new Session("upcoming-selection-refresh", "60");
        s.ok("lesson add st/1 d/today t/08:00");
        s.ok("lesson add st/1 d/today t/09:00");
        UUID first = s.lesson(0).getId();
        UUID second = s.lesson(1).getId();
        s.ok("lesson list st/1");
        check(s.ok("lesson done 1").contains("Lesson numbers have changed"), "changed selection visibly renumbers");
        check(s.model.getFilteredLessonList().size() == 1
                        && s.model.getFilteredLessonList().get(0).getId().equals(second),
                "remaining displayed number refers to the other lesson");
        s.ok("lesson missed 1");
        check(s.lesson(0).getId().equals(first) && s.lesson(0).getStatus() == LessonStatus.COMPLETED
                        && s.lesson(1).getId().equals(second) && s.lesson(1).getStatus() == LessonStatus.MISSED,
                "refreshed number mutates only the intended lesson");
        s.reopen();
        s.select();
        check(s.model.getFilteredLessonList().size() == 2, "all-history selection includes completed and missed");
        s.amount("60", 1);
    }

    static void corrections() throws Exception {
        Session s = new Session("optional-contact-and-homework-feedback", "60");
        s.ok("edit 1 e/alice@example.com v/Old tuition venue");
        UUID studentId = s.model.getAddressBook().getPersonList().get(0).getId();
        s.ok("lesson add st/1 d/today t/09:00");
        s.ok("edit 1 e/");
        check(s.model.getAddressBook().getPersonList().get(0).getEmail().isEmpty(), "empty e/ clears email");
        check(s.model.getAddressBook().getPersonList().get(0).getVenue().isPresent(), "omitted venue remains");
        s.ok("edit 1 v/");
        check(s.model.getAddressBook().getPersonList().get(0).getVenue().isEmpty(), "empty v/ clears venue");
        check(s.lesson(0).getVenue().isPresent(), "existing lesson retains venue snapshot");
        check(s.model.getAddressBook().getPersonList().get(0).getId().equals(studentId)
                        && s.lesson(0).getStudentId().orElseThrow().equals(studentId),
                "contact corrections retain student and linked lesson IDs");
        s.reject("edit 1 n/");
        s.reject("edit 1 e/ e/");
        s.ok("hw add st/1 desc/Algebra due/tmr");
        s.ok("hw list");
        UUID hwId = s.model.getAddressBook().getHomeworkList().get(0).getId();
        s.reject("hw feedback 1 f/Too early");
        s.ok("hw feedback 1 f/");
        check(s.model.getAddressBook().getHomeworkList().get(0).getStatus() == HomeworkStatus.ASSIGNED,
                "clearing absent feedback retains ASSIGNED status");
        s.ok("hw status 1 s/submitted");
        s.ok("hw feedback 1 f/For the wrong assignment");
        s.ok("hw feedback 1 f/");
        s.ok("hw feedback 1 f/");
        check(s.model.getAddressBook().getHomeworkList().get(0).getFeedback().isEmpty()
                        && s.model.getAddressBook().getHomeworkList().get(0).getStatus() == HomeworkStatus.SUBMITTED
                        && s.model.getAddressBook().getHomeworkList().get(0).getId().equals(hwId),
                "feedback clearing retains status and assignment identity");
        s.reopen();
        check(s.model.getAddressBook().getPersonList().get(0).getEmail().isEmpty()
                        && s.model.getAddressBook().getPersonList().get(0).getVenue().isEmpty()
                        && s.model.getAddressBook().getHomeworkList().get(0).getFeedback().isEmpty(),
                "cleared optional fields and feedback survive reopening");
        Session unicode = new Session("unicode-search", "60");
        unicode.ok("add n/Işık p/92345678 l/S3 s/Math r/50");
        unicode.ok("find ışık");
        check(unicode.model.getFilteredPersonList().size() == 1
                        && unicode.model.getFilteredPersonList().get(0).getName().fullName.equals("Işık"),
                "dotless-i search matches stored student");
    }

    public static void main(String[] args) throws Exception {
        root = Path.of(args[0]);
        Files.createDirectories(root);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(root.resolve("transcript.txt")))) {
            log = writer;
            log.println("Target: " + args[1] + "; Java " + System.getProperty("java.version")
                    + "; local date " + LocalDate.now() + "; zone " + java.time.ZoneId.systemDefault());
            log.println("Method: command journeys through packaged LogicManager, parser, model and real JSON storage.");
            log.println("These are programmatic packaged-JAR checks, not claimed as GUI clicks.");
            completion();
            missed();
            charging();
            selection();
            corrections();
            log.println("\nSUMMARY: " + checks + " assertions; " + commands + " commands; 0 failures.");
            System.out.println("PASS: " + checks + " assertions; " + commands + " commands; " + args[1]);
        }
    }
}
