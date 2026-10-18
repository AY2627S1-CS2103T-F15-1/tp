package seedu.address.storage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.finance.LedgerEntry;

/** Serializes immutable events as exact decimal strings and persistent links. */
class JsonAdaptedLedgerEntry {
    private final String id;
    private final String studentId;
    private final String kind;
    private final String date;
    private final String amount;
    private final String lessonId;
    private final String reversalOf;
    private final String hourlyRate;
    private final Integer durationMinutes;
    private final String description;
    private final boolean overridden;

    /** Creates an adapter from persisted event fields. */
    @JsonCreator
    public JsonAdaptedLedgerEntry(@JsonProperty("id") String id, @JsonProperty("studentId") String studentId,
            @JsonProperty("kind") String kind, @JsonProperty("date") String date,
            @JsonProperty("amount") String amount, @JsonProperty("lessonId") String lessonId,
            @JsonProperty("reversalOf") String reversalOf, @JsonProperty("hourlyRate") String hourlyRate,
            @JsonProperty("durationMinutes") Integer durationMinutes, @JsonProperty("description") String description,
            @JsonProperty("overridden") boolean overridden) {
        this.id = id;
        this.studentId = studentId;
        this.kind = kind;
        this.date = date;
        this.amount = amount;
        this.lessonId = lessonId;
        this.reversalOf = reversalOf;
        this.hourlyRate = hourlyRate;
        this.durationMinutes = durationMinutes;
        this.description = description;
        this.overridden = overridden;
    }

    /** Copies every event field without losing decimal precision. */
    public JsonAdaptedLedgerEntry(LedgerEntry entry) {
        this(entry.id().toString(), entry.studentId().toString(), entry.kind().name(), entry.date().toString(),
                entry.amount().toPlainString(), entry.lessonId() == null ? null : entry.lessonId().toString(),
                entry.reversalOf() == null ? null : entry.reversalOf().toString(),
                entry.hourlyRate() == null ? null : entry.hourlyRate().toPlainString(), entry.durationMinutes(),
                entry.description(), entry.overridden());
    }

    /** Restores and validates local event fields; root loading also verifies all links. */
    public LedgerEntry toModelType() throws IllegalValueException {
        try {
            return new LedgerEntry(UUID.fromString(id), UUID.fromString(studentId), LedgerEntry.Kind.valueOf(kind),
                    LocalDate.parse(date), new BigDecimal(amount), lessonId == null ? null : UUID.fromString(lessonId),
                    reversalOf == null ? null : UUID.fromString(reversalOf),
                    hourlyRate == null ? null : new BigDecimal(hourlyRate), durationMinutes, description, overridden);
        } catch (RuntimeException e) {
            throw new IllegalValueException("Invalid financial event: " + e.getMessage());
        }
    }
}
