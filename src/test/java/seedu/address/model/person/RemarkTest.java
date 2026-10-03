package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {
    @Test
    public void constructor_acceptsEmptyAndFreeText_rejectsNull() {
        assertEquals("", new Remark("").value);
        assertEquals("  Anything! 中文  ", new Remark("  Anything! 中文  ").toString());
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void equalsAndHashCode() {
        Remark remark = new Remark("Swimming");
        assertEquals(remark, new Remark("Swimming"));
        assertEquals(remark.hashCode(), new Remark("Swimming").hashCode());
        assertNotEquals(remark, new Remark("Running"));
        assertNotEquals(remark, null);
        assertNotEquals(remark, "Swimming");
    }
}
