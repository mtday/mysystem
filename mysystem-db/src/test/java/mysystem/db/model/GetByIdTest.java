package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;

/**
 * Perform testing on the {@link GetById} class.
 */
public class GetByIdTest {
    @Test
    public void testCompareTo() {
        final GetById a = new GetById.Builder(DataType.COMPANY, 1).build();
        final GetById b = new GetById.Builder(DataType.COMPANY, 1, 2).build();
        final GetById c = new GetById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();
        final GetById d = new GetById.Builder(DataType.COMPANY, 1).setActive(true).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, b.compareTo(d));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(1, c.compareTo(d));
        assertEquals(1, d.compareTo(a));
        assertEquals(-1, d.compareTo(b));
        assertEquals(-1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
    }

    @Test
    public void testEquals() {
        final GetById a = new GetById.Builder(DataType.COMPANY, 1).build();
        final GetById b = new GetById.Builder(DataType.COMPANY, 1, 2).build();
        final GetById c = new GetById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();
        final GetById d = new GetById.Builder(DataType.COMPANY, 1).setActive(true).build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(a.equals(c));
        assertFalse(a.equals(d));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
        assertFalse(b.equals(c));
        assertFalse(b.equals(d));
        assertFalse(c.equals(a));
        assertFalse(c.equals(b));
        assertTrue(c.equals(c));
        assertFalse(c.equals(d));
        assertFalse(d.equals(a));
        assertFalse(d.equals(b));
        assertFalse(d.equals(c));
        assertTrue(d.equals(d));
    }

    @Test
    public void testHashCode() {
        final GetById a = new GetById.Builder(DataType.COMPANY, 1).build();
        final GetById b = new GetById.Builder(DataType.COMPANY, 1, 2).build();
        final GetById c = new GetById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();
        final GetById d = new GetById.Builder(DataType.COMPANY, 1).setActive(true).build();

        assertEquals(-790717145, a.hashCode());
        assertEquals(-790717071, b.hashCode());
        assertEquals(-790716997, c.hashCode());
        assertEquals(-790715914, d.hashCode());
    }

    @Test
    public void testToJson() {
        final GetById a = new GetById.Builder(DataType.COMPANY, 1).build();
        final GetById b = new GetById.Builder(DataType.COMPANY, 1, 2).build();
        final GetById c = new GetById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();
        final GetById d = new GetById.Builder(DataType.COMPANY, 1).setActive(true).build();

        assertEquals("{\"dataType\":\"COMPANY\",\"ids\":[1]}", a.toJson().toString());
        assertEquals("{\"dataType\":\"COMPANY\",\"ids\":[1,2]}", b.toJson().toString());
        assertEquals("{\"dataType\":\"COMPANY\",\"ids\":[2,3]}", c.toJson().toString());
        assertEquals("{\"dataType\":\"COMPANY\",\"active\":true,\"ids\":[1]}", d.toJson().toString());
    }

    @Test
    public void testToString() {
        final GetById a = new GetById.Builder(DataType.COMPANY, 1).build();
        final GetById b = new GetById.Builder(DataType.COMPANY, 1, 2).build();
        final GetById c = new GetById.Builder(DataType.COMPANY, Arrays.asList(2, 3)).build();
        final GetById d = new GetById.Builder(DataType.COMPANY, 1).setActive(true).build();

        assertEquals("GetById[dataType=COMPANY,ids=[1],active=Optional.empty]", a.toString());
        assertEquals("GetById[dataType=COMPANY,ids=[1, 2],active=Optional.empty]", b.toString());
        assertEquals("GetById[dataType=COMPANY,ids=[2, 3],active=Optional.empty]", c.toString());
        assertEquals("GetById[dataType=COMPANY,ids=[1],active=Optional[true]]", d.toString());
    }

    @Test
    public void testBuilderAdd() {
        final GetById company =
                new GetById.Builder(DataType.COMPANY).add(1).add(Arrays.asList(2, 3)).setActive(true).build();
        assertEquals("GetById[dataType=COMPANY,ids=[1, 2, 3],active=Optional[true]]", company.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoIds() {
        new GetById.Builder(DataType.COMPANY).build();
    }
}
