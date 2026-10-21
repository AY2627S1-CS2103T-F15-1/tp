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

    @JsonCreator
    public JsonAdaptedLedgerEntry(@JsonProperty("id") String id, @JsonProperty("studentId") String studentId,
            @JsonProperty("lessonId") String lessonId, @JsonProperty("kind") String kind,
            @JsonProperty("amount") String amount, @JsonProperty("date") String date) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.kind = kind;
        this.amount = amount;
        this.date = date;
    }

    public JsonAdaptedLedgerEntry(LedgerEntry entry) {
        this(entry.id().toString(), entry.studentId().toString(),
                entry.lessonId() == null ? null : entry.lessonId().toString(),
                entry.kind().name(), entry.amount().toPlainString(), entry.date().toString());
    }

    /** Validates every field before restoring a financial record. */
    public LedgerEntry toModelType() throws IllegalValueException {
        if (id == null || studentId == null
                || kind == null || amount == null || date == null) {
            throw new IllegalValueException("Ledger entry has a missing field.");
        }
        try {
            return new LedgerEntry(UUID.fromString(id), UUID.fromString(studentId),
                    lessonId == null ? null : UUID.fromString(lessonId),
                    LedgerEntry.Kind.valueOf(kind), new BigDecimal(amount), LocalDate.parse(date));
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new IllegalValueException("Invalid ledger entry: " + e.getMessage());
        }
    }
}
