package seedu.address.testutil;

import java.time.LocalDate;
import java.util.UUID;

import seedu.address.model.homework.Homework;
import seedu.address.model.homework.HomeworkStatus;
import seedu.address.model.person.Person;

/**
 * A utility class to help with building Homework objects.
 */
public class HomeworkBuilder {

    public static final String DEFAULT_DESCRIPTION = "Algebra Q1-10";
    public static final LocalDate DEFAULT_DUE_DATE = LocalDate.of(2030, 1, 12);

    private UUID studentId;
    private String description;
    private LocalDate dueDate;
    private HomeworkStatus status;
    private String feedback;

    /**
     * Creates a {@code HomeworkBuilder} for {@code student} with the default details: assigned, without feedback.
     */
    public HomeworkBuilder(Person student) {
        studentId = student.getId();
        description = DEFAULT_DESCRIPTION;
        dueDate = DEFAULT_DUE_DATE;
        status = HomeworkStatus.ASSIGNED;
    }

    /**
     * Sets the description of the {@code Homework} that we are building.
     */
    public HomeworkBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the due date of the {@code Homework} that we are building.
     */
    public HomeworkBuilder withDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    /**
     * Sets the status of the {@code Homework} that we are building.
     */
    public HomeworkBuilder withStatus(HomeworkStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the feedback of the {@code Homework} that we are building.
     */
    public HomeworkBuilder withFeedback(String feedback) {
        this.feedback = feedback;
        return this;
    }

    public Homework build() {
        return new Homework(UUID.randomUUID(), studentId, description, dueDate, status, feedback);
    }
}
