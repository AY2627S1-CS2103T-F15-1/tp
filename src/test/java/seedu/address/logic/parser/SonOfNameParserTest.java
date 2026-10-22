package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.model.person.Person;
import seedu.address.model.person.Subject;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/** Checks that a name or a venue may contain the word s/o although s/ is the prefix of a subject. */
public class SonOfNameParserTest {

    private final AddCommandParser addParser = new AddCommandParser();
    private final EditCommandParser editParser = new EditCommandParser();

    @Test
    public void maskAndUnmask_standAloneSonOf_areInverse() {
        String masked = ParserUtil.maskSonOf(" n/Priya s/o Kumar s/Math");

        assertEquals(" n/Priya s/o Kumar s/Math", ParserUtil.unmaskSonOf(masked));
        assertEquals(1, masked.split("s/", -1).length - 1, "only the subject prefix is left");
    }

    @Test
    public void mask_sonOfInsideAWordOrWithMoreLetters_isNotHidden() {
        assertEquals(" n/Rhys/o s/Math s/oxygen", ParserUtil.maskSonOf(" n/Rhys/o s/Math s/oxygen"));
    }

    @Test
    public void parseAdd_sonOfInName_isPartOfTheName() {
        Person expected = new PersonBuilder().withName("Priya s/o Kumar").withPhone("91234567").withLevel("S3")
                .withSubjects("Math").withRate("50").withoutEmail().withoutVenue().build();

        assertParseSuccess(addParser, " n/Priya s/o Kumar p/91234567 l/S3 s/Math r/50", new AddCommand(expected));
        assertParseSuccess(addParser, " p/91234567 l/S3 s/Math r/50 n/Priya s/o Kumar", new AddCommand(expected));
    }

    @Test
    public void parseAdd_sonOfInVenue_isPartOfTheVenue() {
        Person expected = new PersonBuilder().withName("Amy Bee").withPhone("91234567").withLevel("S3")
                .withSubjects("Math").withRate("50").withoutEmail().withVenue("Blk 5 s/o Tan Home").build();

        assertParseSuccess(addParser, " n/Amy Bee p/91234567 l/S3 s/Math r/50 v/Blk 5 s/o Tan Home",
                new AddCommand(expected));
    }

    @Test
    public void parseAdd_sonOfNextToRealSubjects_keepsTheSubjects() {
        Person expected = new PersonBuilder().withName("Priya s/o Kumar").withPhone("91234567").withLevel("S3")
                .withSubjects("Math", "Physics").withRate("50").withoutEmail().withoutVenue().build();

        assertParseSuccess(addParser, " n/Priya s/o Kumar p/91234567 l/S3 s/Math s/Physics r/50",
                new AddCommand(expected));
    }

    @Test
    public void parseAdd_sonOfAfterASubject_isNotASubject() {
        assertParseFailure(addParser, " n/Amy p/91234567 l/S3 s/Math s/o r/50", Subject.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parseEdit_sonOfInName_isPartOfTheName() {
        EditCommand expected = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName("Priya s/o Kumar").build());

        assertParseSuccess(editParser, "1 n/Priya s/o Kumar", expected);
    }
}
