package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import mysystem.common.serialization.ManifestMapping;

/**
 * Perform testing on the {@link GetAll} class.
 */
public class GetAllTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.COMPANY).setActive(true).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
    }

    @Test
    public void testEquals() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.COMPANY).setActive(true).build();

        assertFalse(a.equals(null));
        assertTrue(a.equals(a));
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertTrue(b.equals(b));
    }

    @Test
    public void testHashCode() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.COMPANY).setActive(true).build();

        assertEquals(1603752026, a.hashCode());
        assertEquals(1603753257, b.hashCode());
    }

    @Test
    public void testToJson() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.COMPANY).setActive(true).build();

        assertEquals("{\"dataType\":\"COMPANY\",\"manifest\":\"GetAll\"}", a.toJson().toString());
        assertEquals("{\"dataType\":\"COMPANY\",\"active\":true,\"manifest\":\"GetAll\"}", b.toJson().toString());
    }

    @Test
    public void testToString() {
        final GetAll a = new GetAll.Builder(DataType.COMPANY).build();
        final GetAll b = new GetAll.Builder(DataType.COMPANY).setActive(true).build();

        assertEquals("GetAll[dataType=COMPANY,active=Optional.empty]", a.toString());
        assertEquals("GetAll[dataType=COMPANY,active=Optional[true]]", b.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoDataType() {
        new GetAll.Builder().build();
    }

    @Test
    public void testFromJson() {
        final GetAll original = new GetAll.Builder(DataType.COMPANY).setActive(true).build();
        final GetAll copy = new GetAll.Builder().fromJson(mapping, original.toJson()).build();

        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testFromJsonNoDataType() {
        final JsonObject json = new JsonParser().parse("{\"manifest\":\"GetAll\"}").getAsJsonObject();
        new GetAll.Builder().fromJson(mapping, json).build();
    }
}
