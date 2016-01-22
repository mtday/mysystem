package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing on the {@link GetAll} class.
 */
public class GetAllTest {
    @Test
    public void testCompareTo() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.USER).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.USER).build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.USER).build();

        assertEquals(1668466781, a.hashCode());
        assertEquals(2614219, b.hashCode());
    }

    @Test
    public void testToString() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.USER).build();

        assertEquals("GetAll[dataType=COMPANY]", a.toString());
        assertEquals("GetAll[dataType=USER]", b.toString());
    }
}
