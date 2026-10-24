package seedu.address.storage;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.finance.LedgerEntry;

/** JSON representation of an immutable ledger record. */
class JsonAdaptedLedgerEntry {
    private final String id;
    private final String studentId;
    private final String lessonId;
    private final String kind;
    private final String amount;
    private final String date;
    private final String notes;

    /** Restores records saved before payment notes were added. */
    public JsonAdaptedLedgerEntry(String id, String studentId, String lessonId, String kind,
            String amount, String date) {
        this(id, studentId, lessonId, kind, amount, date, null);
    }

    /** Restores the persisted fields, defaulting absent notes to null for older records. */
    @JsonCreator
    public JsonAdaptedLedgerEntry(@JsonProperty("id") String id, @JsonProperty("studentId") String studentId,
            @JsonProperty("lessonId") String lessonId, @JsonProperty("kind") String kind,
            @JsonProperty("amount") String amount, @JsonProperty("date") String date,
            @JsonProperty("notes") String notes) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.kind = kind;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
    }

    public JsonAdaptedLedgerEntry(LedgerEntry entry) {
        this(entry.id().toString(), entry.studentId().toString(),
                entry.lessonId() == null ? null : entry.lessonId().toString(),
                entry.kind().name(), entry.amount().toPlainString(), entry.date().toString(), entry.notes());
    }

    /** Validates every field before restoring a financial record. */
    public LedgerEntry toModelType() throws IllegalValueException {
        requireField(id, "id");
        requireField(studentId, "studentId");
        requireField(kind, "kind");
        requireField(amount, "amount");
        requireField(date, "date");
        if ("CHARGE".equals(kind)) {
            requireField(lessonId, "lessonId");
        }
        try {
            return new LedgerEntry(UUID.fromString(id), UUID.fromString(studentId),
                    lessonId == null ? null : UUID.fromString(lessonId),
                    LedgerEntry.Kind.valueOf(kind), new BigDecimal(amount), LocalDate.parse(date), notes);
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new IllegalValueException("Invalid ledger entry: " + e.getMessage());
        }
    }

    private static void requireField(String value, String field) throws IllegalValueException {
        if (value == null) {
            throw new IllegalValueException("Ledger entry field '" + field + "' is missing.");
        }
    }
}
