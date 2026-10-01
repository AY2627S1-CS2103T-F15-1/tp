package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void equals() {
        Remark remark = new Remark("Likes baseball");
        assertEquals(remark, new Remark("Likes baseball"));
        assertNotEquals(remark, new Remark("Likes swimming"));
        assertNotEquals(remark, null);
    }

    @Test
    public void toStringMethod() {
        assertEquals("Likes baseball", new Remark("Likes baseball").toString());
    }
}
