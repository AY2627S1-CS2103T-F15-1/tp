package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

public class CommandAssistTest {

    private static final class StubCommandInput implements CommandDraftTarget {
        private String text = "";

        @Override
        public String getText() {
            return text;
        }

        @Override
        public void setTextAndFocus(String text) {
            this.text = text;
        }

        @Override
        public void clearAndFocus() {
            text = "";
        }
    }

    private final StubCommandInput input = new StubCommandInput();
    private final List<String> help = new ArrayList<>();
    private final CommandAssist assist = new CommandAssist(input, help::add);

    @Test
    public void editField_populatesExistingEditSyntaxForEverySupportedField() {
        assertEquals("edit 3 n/", draftAfter(() -> assist.editField(3, CommandAssist.Field.NAME)));
        assertEquals("edit 3 l/", draftAfter(() -> assist.editField(3, CommandAssist.Field.LEVEL)));
        assertEquals("edit 3 s/", draftAfter(() -> assist.editField(3, CommandAssist.Field.SUBJECT)));
        assertEquals("edit 3 p/", draftAfter(() -> assist.editField(3, CommandAssist.Field.PHONE)));
        assertEquals("edit 3 r/", draftAfter(() -> assist.editField(3, CommandAssist.Field.RATE)));
        assertEquals("edit 3 e/", draftAfter(() -> assist.editField(3, CommandAssist.Field.EMAIL)));
        assertEquals("edit 3 v/", draftAfter(() -> assist.editField(3, CommandAssist.Field.VENUE)));
        assertEquals("remark 3 r/", draftAfter(() -> assist.editField(3, CommandAssist.Field.REMARK)));
    }

    @Test
    public void actions_populateOnlyTheCommandDraftAndDirectHelpToTheOutput() {
        assist.archive(2);
        assertEquals("archive 2", input.getText());
        assist.delete(4);
        assertEquals("delete 4", input.getText());
        assist.addStudent();
        assertEquals("add n/", input.getText());
        assertEquals(List.of("help archive", "help delete", "help add"), help);
    }

    @Test
    public void cancel_clearsOnlyAnUnchangedGeneratedDraft() {
        assist.editField(1, CommandAssist.Field.NAME);
        assist.cancelEditInteraction(1);
        assertEquals("", input.getText());

        assist.editField(1, CommandAssist.Field.PHONE);
        input.setTextAndFocus("user edited");
        assist.cancelEditInteraction(1);
        assertEquals("user edited", input.getText());
    }

    @Test
    public void replaceDraft_discardsAnEarlierGeneratedDraft() {
        assist.editField(1, CommandAssist.Field.NAME);
        input.setTextAndFocus("edit 1 n/Alice");
        assist.editField(1, CommandAssist.Field.PHONE);

        assertEquals("edit 1 p/", input.getText());
    }

    @Test
    public void cancel_afterAnotherActionKeepsTheNewDraft() {
        assist.editField(1, CommandAssist.Field.NAME);
        assist.archive(1);
        assist.cancelEditInteraction(1);

        assertEquals("archive 1", input.getText());
    }

    @Test
    public void cancel_afterOpeningEditWithAnOlderDraft_keepsTheOlderDraft() {
        assist.archive(1);
        assist.beginEditInteraction(1, () -> { });
        assist.cancelEditInteraction(1);

        assertEquals("archive 1", input.getText());
    }

    @Test
    public void editMode_isClosedByCancelNewCardActionAndSuccessfulCommand() {
        AtomicBoolean isClosed = new AtomicBoolean();
        assist.beginEditInteraction(1, () -> isClosed.set(true));

        assist.cancelEditInteraction(1);
        assertTrue(isClosed.getAndSet(false));

        assist.beginEditInteraction(1, () -> isClosed.set(true));
        assist.archive(1);
        assertTrue(isClosed.getAndSet(false));

        assist.beginEditInteraction(1, () -> isClosed.set(true));
        assist.finishInteraction();
        assertTrue(isClosed.get());
    }

    @Test
    public void displayedListChanged_clearsUnchangedGeneratedDraft() {
        assist.editField(1, CommandAssist.Field.NAME);
        assist.displayedListChanged();

        assertEquals("", input.getText());
    }

    @Test
    public void displayedListChanged_keepsUserEditedText() {
        assist.editField(1, CommandAssist.Field.NAME);
        input.setTextAndFocus("edit 1 n/Alice");
        assist.displayedListChanged();

        assertEquals("edit 1 n/Alice", input.getText());
    }

    @Test
    public void displayedListChanged_keepsAddDraftBecauseItHasNoStudentIndex() {
        assist.addStudent();
        assist.displayedListChanged();

        assertEquals("add n/", input.getText());
    }

    private String draftAfter(Runnable action) {
        action.run();
        return input.getText();
    }
}
