package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

/**
 * Perform testing on the {@link DeleteById} class.
 */
public class DeleteByIdTest {
    @Test
    public void testCompareTo() {
        final DeleteById a = new DeleteById.Builder(DataType.COMPANY, 1).build();
        final DeleteById b = new DeleteById.Builder(DataType.COMPANY, 1, 2).build();
        final DeleteById c = new DeleteById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final DeleteById a = new DeleteById.Builder(DataType.COMPANY, 1).build();
        final DeleteById b = new DeleteById.Builder(DataType.COMPANY, 1, 2).build();
        final DeleteById c = new DeleteById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
        assertFalse(b.equals(c));
        assertFalse(c.equals(a));
        assertFalse(c.equals(b));
        assertTrue(c.equals(c));
    }

    @Test
    public void testHashCode() {
        final DeleteById a = new DeleteById.Builder(DataType.COMPANY, 1).build();
        final DeleteById b = new DeleteById.Builder(DataType.COMPANY, 1, 2).build();
        final DeleteById c = new DeleteById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();

        assertEquals(1603752027, a.hashCode());
        assertEquals(1603752029, b.hashCode());
        assertEquals(1603752031, c.hashCode());
    }

    @Test
    public void testToString() {
        final DeleteById a = new DeleteById.Builder(DataType.COMPANY, 1).build();
        final DeleteById b = new DeleteById.Builder(DataType.COMPANY, 1, 2).build();
        final DeleteById c = new DeleteById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();

        assertEquals("DeleteById[dataType=COMPANY,ids=[1]]", a.toString());
        assertEquals("DeleteById[dataType=COMPANY,ids=[1, 2]]", b.toString());
        assertEquals("DeleteById[dataType=COMPANY,ids=[2, 3]]", c.toString());
    }

    @Test
    public void testBuilderAdd() {
        final DeleteById company =
                new DeleteById.Builder(DataType.COMPANY).add(1).add(Arrays.asList(2, 3)).build();
        assertEquals("DeleteById[dataType=COMPANY,ids=[1, 2, 3]]", company.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoIds() {
        new DeleteById.Builder(DataType.COMPANY).build();
    }
}
