package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.finance.LedgerEntry;
import seedu.address.model.homework.Homework;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.task.FollowUpTask;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.TypicalPersons;

public class WorkflowStorageTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static AddressBook workflowBook() {
        AddressBook book = new AddressBook();
        book.addPerson(TypicalPersons.ALICE);
        book.addPerson(TypicalPersons.BOB);
        Lesson lesson = new LessonBuilder().build().withStudent(TypicalPersons.ALICE)
                .withSeriesId(UUID.randomUUID());
        book.addLesson(lesson);
        Homework homework = Homework.assign(TypicalPersons.ALICE.getId(), "Worksheet", LocalDate.now());
        book.addHomework(homework);
        LedgerEntry payment = LedgerEntry.payment(TypicalPersons.ALICE.getId(), BigDecimal.TEN, LocalDate.now(),
                null);
        book.addLedgerEntry(payment);
        book.addLedgerEntry(LedgerEntry.charge(lesson, TypicalPersons.ALICE, null));
        UUID alice = TypicalPersons.ALICE.getId();
        book.addTask(task(alice, FollowUpTask.LinkType.STUDENT, alice));
        book.addTask(task(alice, FollowUpTask.LinkType.LESSON, lesson.getId()));
        book.addTask(task(alice, FollowUpTask.LinkType.HOMEWORK, homework.getId()));
        book.addTask(task(alice, FollowUpTask.LinkType.PAYMENT, payment.id()));
        book.setTravelBufferMinutes(45);
        return book;
    }

    private static FollowUpTask task(UUID student, FollowUpTask.LinkType type, UUID target) {
        return new FollowUpTask(UUID.randomUUID(), student, type, target, "Follow up", null, false);
    }

    private static ObjectNode jsonOf(AddressBook book) throws Exception {
        return (ObjectNode) MAPPER.readTree(JsonUtil.toJsonString(new JsonSerializableAddressBook(book)));
    }

    private static AddressBook load(JsonNode tree) throws Exception {
        return JsonUtil.fromJsonString(MAPPER.writeValueAsString(tree), JsonSerializableAddressBook.class)
                .toModelType();
    }

    private static ObjectNode taskAt(ObjectNode tree, int index) {
        return (ObjectNode) tree.get("tasks").get(index);
    }

    private static ObjectNode lessonAt(ObjectNode tree, int index) {
        return (ObjectNode) tree.get("lessons").get(index);
    }

    @Test
    public void roundTrip_tasksSeriesAndBuffer_preserveEveryField() throws Exception {
        AddressBook book = workflowBook();
        AddressBook restored = load(jsonOf(book));
        assertEquals(book, restored);
        assertEquals(45, restored.getTravelBufferMinutes());
        assertEquals(4, restored.getTaskList().size());
        assertTrue(restored.getLessonList().getFirst().getSeriesId().isPresent());
    }

    @Test
    public void load_invalidTasks_rejectWithoutPartialData() throws Exception {
        Map<String, Consumer<ObjectNode>> invalidEdits = new LinkedHashMap<>();
        invalidEdits.put("orphan target", tree -> taskAt(tree, 1).put("targetId", UUID.randomUUID().toString()));
        invalidEdits.put("unknown student",
                tree -> taskAt(tree, 0).put("studentId", UUID.randomUUID().toString()));
        invalidEdits.put("another student's lesson",
                tree -> taskAt(tree, 1).put("studentId", tree.get("persons").get(1).get("id").asText()));
        invalidEdits.put("payment link to a charge", tree -> taskAt(tree, 3).put("targetId", chargeId(tree)));
        invalidEdits.put("student link to a lesson",
                tree -> taskAt(tree, 0).put("targetId", tree.get("lessons").get(0).get("id").asText()));
        invalidEdits.put("duplicate task id",
                tree -> ((ArrayNode) tree.get("tasks")).add(tree.get("tasks").get(0).deepCopy()));
        invalidEdits.put("missing completion flag", tree -> taskAt(tree, 0).remove("done"));
        invalidEdits.put("blank description", tree -> taskAt(tree, 0).put("description", "   "));
        invalidEdits.put("overlong description", tree -> taskAt(tree, 0).put("description", "x".repeat(201)));
        invalidEdits.put("unknown link type", tree -> taskAt(tree, 0).put("linkType", "UNKNOWN"));
        invalidEdits.put("impossible due date", tree -> taskAt(tree, 0).put("dueDate", "2026-02-30"));
        invalidEdits.put("malformed id", tree -> taskAt(tree, 0).put("id", "not-a-uuid"));
        invalidEdits.put("null task", tree -> ((ArrayNode) tree.get("tasks")).addNull());
        for (Map.Entry<String, Consumer<ObjectNode>> edit : invalidEdits.entrySet()) {
            ObjectNode tree = jsonOf(workflowBook());
            edit.getValue().accept(tree);
            assertThrows(IllegalValueException.class, () -> load(tree), edit.getKey());
        }
    }

    private static String chargeId(ObjectNode tree) {
        for (JsonNode entry : tree.get("ledgerEntries")) {
            if ("CHARGE".equals(entry.get("kind").asText())) {
                return entry.get("id").asText();
            }
        }
        throw new IllegalStateException("The fixture must contain a charge.");
    }

    @Test
    public void load_travelBuffer_enforcesBoundsAndDefault() throws Exception {
        for (int minutes : new int[] {0, 180}) {
            ObjectNode tree = jsonOf(workflowBook());
            tree.put("travelBufferMinutes", minutes);
            assertEquals(minutes, load(tree).getTravelBufferMinutes());
        }
        for (int minutes : new int[] {-1, 181}) {
            ObjectNode tree = jsonOf(workflowBook());
            tree.put("travelBufferMinutes", minutes);
            assertThrows(IllegalValueException.class, () -> load(tree), "buffer " + minutes);
        }
        ObjectNode absent = jsonOf(workflowBook());
        absent.remove("travelBufferMinutes");
        assertEquals(30, load(absent).getTravelBufferMinutes());
    }

    @Test
    public void load_malformedSeriesId_rejects() throws Exception {
        ObjectNode tree = jsonOf(workflowBook());
        lessonAt(tree, 0).put("seriesId", "not-a-uuid");
        assertThrows(IllegalValueException.class, () -> load(tree));
    }

    @Test
    public void load_schemasBeforeWorkflows_defaultTasksSeriesAndBuffer() throws Exception {
        for (int version = StorageSchema.MINIMUM_VERSION; version < StorageSchema.CURRENT_VERSION; version++) {
            ObjectNode tree = jsonOf(workflowBook());
            tree.put("schemaVersion", version);
            tree.remove("tasks");
            tree.remove("travelBufferMinutes");
            lessonAt(tree, 0).remove("seriesId");
            AddressBook restored = load(tree);
            assertTrue(restored.getTaskList().isEmpty(), "schema " + version);
            assertEquals(30, restored.getTravelBufferMinutes(), "schema " + version);
            assertTrue(restored.getLessonList().getFirst().getSeriesId().isEmpty(), "schema " + version);
            assertEquals(2, restored.getLedgerEntries().size(), "schema " + version);
        }
    }

    @Test
    public void load_schemaNewerThanCurrent_rejects() throws Exception {
        ObjectNode tree = jsonOf(workflowBook());
        tree.put("schemaVersion", StorageSchema.CURRENT_VERSION + 1);
        assertThrows(IllegalValueException.class, () -> load(tree));
    }
}
