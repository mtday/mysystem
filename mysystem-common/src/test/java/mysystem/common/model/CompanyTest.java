package mysystem.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

/**
 * Perform testing of the {@link Company} class and builder.
 */
public class CompanyTest {
    @Test
    public void testCompareTo() {
        final Company a = new Company.Builder().setId(1).setName("a").build();
        final Company b = new Company.Builder().setId(1).setName("a").setActive(false).build();
        final Company c = new Company.Builder().setId(2).setName("b").build();
        final Company d = new Company.Builder().setName("b").build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, a.compareTo(d));
        assertEquals(-1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, b.compareTo(d));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(1, c.compareTo(d));
        assertEquals(-1, d.compareTo(a));
        assertEquals(-1, d.compareTo(b));
        assertEquals(-1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
    }

    @Test
    public void testEquals() {
        final Company a = new Company.Builder().setId(1).setName("a").build();
        final Company b = new Company.Builder().setId(1).setName("a").setActive(false).build();
        final Company c = new Company.Builder().setId(2).setName("b").build();
        final Company d = new Company.Builder().setName("b").build();

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
        final Company a = new Company.Builder().setId(1).setName("a").build();
        final Company b = new Company.Builder().setId(1).setName("a").setActive(false).build();
        final Company c = new Company.Builder().setId(2).setName("b").build();
        final Company d = new Company.Builder().setName("b").build();

        assertEquals(866059, a.hashCode());
        assertEquals(866060, b.hashCode());
        assertEquals(867465, c.hashCode());
        assertEquals(864727, d.hashCode());
    }

    @Test
    public void testToJson() {
        final Company a = new Company.Builder().setId(1).setName("a").build();
        final Company b = new Company.Builder().setId(1).setName("a").setActive(false).build();
        final Company c = new Company.Builder().setId(2).setName("b").build();
        final Company d = new Company.Builder().setName("b").build();

        assertEquals("{\"id\":1,\"name\":\"a\",\"active\":true}", a.toJson().toString());
        assertEquals("{\"id\":1,\"name\":\"a\",\"active\":false}", b.toJson().toString());
        assertEquals("{\"id\":2,\"name\":\"b\",\"active\":true}", c.toJson().toString());
        assertEquals("{\"name\":\"b\",\"active\":true}", d.toJson().toString());
    }

    @Test
    public void testToString() {
        final Company a = new Company.Builder().setId(1).setName("a").build();
        final Company b = new Company.Builder().setId(1).setName("a").setActive(false).build();
        final Company c = new Company.Builder().setId(2).setName("b").build();
        final Company d = new Company.Builder().setName("b").build();

        assertEquals("Company[id=Optional[1],name=a,active=true]", a.toString());
        assertEquals("Company[id=Optional[1],name=a,active=false]", b.toString());
        assertEquals("Company[id=Optional[2],name=b,active=true]", c.toString());
        assertEquals("Company[id=Optional.empty,name=b,active=true]", d.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderWithNoName() {
        new Company.Builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithEmptyName() {
        new Company.Builder().setName("").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderWithBlankName() {
        new Company.Builder().setName("   ").build();
    }

    @Test
    public void testBuilderFromJson() {
        final JsonParser jsonParser = new JsonParser();
        final JsonObject ja = jsonParser.parse("{\"id\":1,\"name\":\"a\",\"active\":true}").getAsJsonObject();
        final JsonObject jb = jsonParser.parse("{\"id\":1,\"name\":\"a\",\"active\":false}").getAsJsonObject();
        final JsonObject jc = jsonParser.parse("{\"id\":2,\"name\":\"b\",\"active\":true}").getAsJsonObject();
        final JsonObject jd = jsonParser.parse("{\"name\":\"b\"}").getAsJsonObject();

        final Company ca = new Company.Builder().setId(1).setName("a").build();
        final Company cb = new Company.Builder().setId(1).setName("a").setActive(false).build();
        final Company cc = new Company.Builder().setId(2).setName("b").build();
        final Company cd = new Company.Builder().setName("b").build();

        assertEquals(ca, new Company.Builder().fromJson(ja).build());
        assertEquals(cb, new Company.Builder().fromJson(jb).build());
        assertEquals(cc, new Company.Builder().fromJson(jc).build());
        assertEquals(cd, new Company.Builder().fromJson(jd).build());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoName() {
        new Company.Builder().fromJson(new JsonParser().parse("{\"active\":true}").getAsJsonObject()).build();
    }

    @Test
    public void testBuilderWithCompany() {
        final Company a = new Company.Builder().setId(1).setName("a").setActive(false).build();
        final Company b = new Company.Builder(a).build();

        assertEquals(a, b);
    }
}
