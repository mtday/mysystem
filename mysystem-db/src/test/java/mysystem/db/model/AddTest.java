package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import mysystem.common.model.Company;
import mysystem.common.serialization.ManifestMapping;

import java.util.Arrays;

/**
 * Perform testing on the {@link Add} class.
 */
public class AddTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() {
        final Company ca = new Company.Builder().setName("a").build();
        final Company cb = new Company.Builder().setName("b").build();
        final Company cc = new Company.Builder().setName("c").build();
        final Company cd = new Company.Builder().setId(1).setName("a").build();

        final Add<Company> a = new Add.Builder<>(DataType.COMPANY, ca).build();
        final Add<Company> b = new Add.Builder<>(DataType.COMPANY, ca, cb).build();
        final Add<Company> c = new Add.Builder<>(DataType.COMPANY, Arrays.asList(cb, cc)).build();
        final Add<Company> d = new Add.Builder<>(DataType.COMPANY, cd).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(-1, b.compareTo(d));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));
        assertEquals(1, d.compareTo(a));
        assertEquals(1, d.compareTo(b));
        assertEquals(1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
    }

    @Test
    public void testEquals() {
        final Company ca = new Company.Builder().setName("a").build();
        final Company cb = new Company.Builder().setName("b").build();
        final Company cc = new Company.Builder().setName("c").build();
        final Company cd = new Company.Builder().setId(1).setName("a").build();

        final Add<Company> a = new Add.Builder<>(DataType.COMPANY, ca).build();
        final Add<Company> b = new Add.Builder<>(DataType.COMPANY, ca, cb).build();
        final Add<Company> c = new Add.Builder<>(DataType.COMPANY, Arrays.asList(cb, cc)).build();
        final Add<Company> d = new Add.Builder<>(DataType.COMPANY, cd).build();

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
        final Company ca = new Company.Builder().setName("a").build();
        final Company cb = new Company.Builder().setName("b").build();
        final Company cc = new Company.Builder().setName("c").build();
        final Company cd = new Company.Builder().setId(1).setName("a").build();

        final Add<Company> a = new Add.Builder<>(DataType.COMPANY, ca).build();
        final Add<Company> b = new Add.Builder<>(DataType.COMPANY, ca, cb).build();
        final Add<Company> c = new Add.Builder<>(DataType.COMPANY, Arrays.asList(cb, cc)).build();
        final Add<Company> d = new Add.Builder<>(DataType.COMPANY, cd).build();

        assertEquals(1604616716, a.hashCode());
        assertEquals(1605481443, b.hashCode());
        assertEquals(1605481517, c.hashCode());
        assertEquals(1604618085, d.hashCode());
    }

    @Test
    public void testToJson() {
        final Company ca = new Company.Builder().setName("a").build();
        final Company cb = new Company.Builder().setName("b").build();
        final Company cc = new Company.Builder().setName("c").build();
        final Company cd = new Company.Builder().setId(1).setName("a").build();

        final Add<Company> a = new Add.Builder<>(DataType.COMPANY, ca).build();
        final Add<Company> b = new Add.Builder<>(DataType.COMPANY, ca, cb).build();
        final Add<Company> c = new Add.Builder<>(DataType.COMPANY, Arrays.asList(cb, cc)).build();
        final Add<Company> d = new Add.Builder<>(DataType.COMPANY, cd).build();

        assertEquals("{\"dataType\":\"COMPANY\",\"models\":[{\"name\":\"a\",\"active\":true,\"manifest\":\"Company\"}],"
                + "\"manifest\":\"Add\"}", a.toJson().toString());
        assertEquals("{\"dataType\":\"COMPANY\",\"models\":[{\"name\":\"a\",\"active\":true,"
                + "\"manifest\":\"Company\"},{\"name\":\"b\",\"active\":true,\"manifest\":\"Company\"}],"
                + "\"manifest\":\"Add\"}", b.toJson().toString());
        assertEquals("{\"dataType\":\"COMPANY\",\"models\":[{\"name\":\"b\",\"active\":true,"
                + "\"manifest\":\"Company\"},{\"name\":\"c\",\"active\":true,\"manifest\":\"Company\"}],"
                + "\"manifest\":\"Add\"}", c.toJson().toString());
        assertEquals("{\"dataType\":\"COMPANY\",\"models\":[{\"id\":1,\"name\":\"a\",\"active\":true,"
                + "\"manifest\":\"Company\"}],\"manifest\":\"Add\"}", d.toJson().toString());
    }

    @Test
    public void testToString() {
        final Company ca = new Company.Builder().setName("a").build();
        final Company cb = new Company.Builder().setName("b").build();
        final Company cc = new Company.Builder().setName("c").build();
        final Company cd = new Company.Builder().setId(1).setName("a").build();

        final Add<Company> a = new Add.Builder<>(DataType.COMPANY, ca).build();
        final Add<Company> b = new Add.Builder<>(DataType.COMPANY, ca, cb).build();
        final Add<Company> c = new Add.Builder<>(DataType.COMPANY, Arrays.asList(cb, cc)).build();
        final Add<Company> d = new Add.Builder<>(DataType.COMPANY, cd).build();

        assertEquals("Add[dataType=COMPANY,models=[Company[id=Optional.empty,name=a,active=true]]]", a.toString());
        assertEquals("Add[dataType=COMPANY,models=[Company[id=Optional.empty,name=a,active=true], "
                + "Company[id=Optional.empty,name=b,active=true]]]", b.toString());
        assertEquals("Add[dataType=COMPANY,models=[Company[id=Optional.empty,name=b,active=true], "
                + "Company[id=Optional.empty,name=c,active=true]]]", c.toString());
        assertEquals("Add[dataType=COMPANY,models=[Company[id=Optional[1],name=a,active=true]]]", d.toString());
    }

    @Test
    public void testBuilderAdd() {
        final Company company = new Company.Builder().setName("a").build();
        final Add<Company> add = new Add.Builder<Company>(DataType.COMPANY).add(company).build();

        assertEquals(DataType.COMPANY, add.getDataType());
        assertEquals(1, add.getModels().size());
        assertTrue(add.getModels().contains(company));
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoModels() {
        new Add.Builder(DataType.COMPANY).build();
    }

    @Test
    public void testJsonRoundTrip() {
        final Company company = new Company.Builder().setName("a").build();
        final Add<Company> original = new Add.Builder<Company>(DataType.COMPANY).add(company).build();
        final Add<Company> copy = new Add.Builder<Company>().fromJson(mapping, original.toJson()).build();
        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoDataType() {
        final Company company = new Company.Builder().setName("a").build();
        new Add.Builder<Company>().add(company).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testFromJsonNoDataType() {
        final String jsonStr = "{\"models\":[{\"id\":1,\"name\":\"a\",\"active\":true,\"manifest\":\"Company\"}],"
                + "\"manifest\":\"Add\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Add.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testFromJsonNoModels() {
        final String jsonStr = "{\"dataType\":\"COMPANY\",\"manifest\":\"Add\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Add.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testFromJsonUnrecognizedManifest() {
        final String jsonStr =
                "{\"dataType\":\"COMPANY\",\"models\":[{\"id\":1,\"name\":\"a\",\"active\":true,"
                        + "\"manifest\":\"Unrecognized\"}],"
                        + "\"manifest\":\"Add\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Add.Builder().fromJson(mapping, json).build();
    }
}
