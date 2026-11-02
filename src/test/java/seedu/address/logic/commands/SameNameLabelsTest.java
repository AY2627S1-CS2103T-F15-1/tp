package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonStatus;
import seedu.address.model.person.Person;
import seedu.address.testutil.LessonBuilder;
import seedu.address.testutil.PersonBuilder;

/** Checks that finance and lesson-list replies tell students with the same name apart. */
public class SameNameLabelsTest {
    private static final String SECOND_LABEL = "phone ending 2222";

    private final Model model = new ModelManager();
    private final Person first = new PersonBuilder().withName("Sam Tan").withPhone("91111111").build();
    private final Person second = new PersonBuilder().withName("Sam Tan").withPhone("92222222").build();
    private final Index secondIndex = Index.fromOneBased(2);

    private void assertLabelled(Command command) throws Exception {
        assertTrue(command.execute(model).getFeedbackToUser().contains(SECOND_LABEL));
    }

    @Test
    public void execute_sameNameStudents_labelledInFinanceAndLessonReplies() throws Exception {
        model.addPerson(first);
        model.addPerson(second);
        Lesson lesson = new LessonBuilder().withStudent(second).withDate(LocalDate.now().minusDays(1))
                .withStatus(LessonStatus.COMPLETED).build();
        model.addLesson(lesson);

        assertLabelled(new PaymentCommand(secondIndex, BigDecimal.TEN));
        assertLabelled(new PaymentListCommand(null));
        assertLabelled(new BalanceCommand(secondIndex));
        assertLabelled(new LessonListCommand(secondIndex, true));
        assertLabelled(new WithdrawCommand(secondIndex, BigDecimal.ONE));
        assertLabelled(new ChargeCommand(Index.fromOneBased(1), BigDecimal.TEN));
    }
}
