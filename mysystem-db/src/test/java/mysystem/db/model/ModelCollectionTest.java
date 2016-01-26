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
 * Perform testing on the {@link ModelCollection} class.
 */
public class ModelCollectionTest {
    private final ManifestMapping mapping = new ManifestMapping();

    @Test
    public void testCompareTo() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final ModelCollection<Company> a = new ModelCollection.Builder<Company>().build();
        final ModelCollection<Company> b = new ModelCollection.Builder<>(companyA, companyB).build();
        final ModelCollection<Company> c = new ModelCollection.Builder<>(companyC).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-2, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(2, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final ModelCollection<Company> a = new ModelCollection.Builder<Company>().build();
        final ModelCollection<Company> b = new ModelCollection.Builder<>(companyA, companyB).build();
        final ModelCollection<Company> c = new ModelCollection.Builder<>(companyC).build();

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
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final ModelCollection<Company> a = new ModelCollection.Builder<Company>().build();
        final ModelCollection<Company> b = new ModelCollection.Builder<>(companyA, companyB).build();
        final ModelCollection<Company> c = new ModelCollection.Builder<>(companyC).build();

        assertEquals(629, a.hashCode());
        assertEquals(1730046, b.hashCode());
        assertEquals(865393, c.hashCode());
    }

    @Test
    public void testToJson() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final ModelCollection<Company> a = new ModelCollection.Builder<Company>().build();
        final ModelCollection<Company> b = new ModelCollection.Builder<>(companyA, companyB).build();
        final ModelCollection<Company> c = new ModelCollection.Builder<>(companyC).build();

        assertEquals("{\"models\":[],\"manifest\":\"ModelCollection\"}", a.toJson().toString());
        assertEquals(
                "{\"models\":[{\"name\":\"a\",\"active\":true,\"manifest\":\"Company\"},{\"name\":\"b\","
                        + "\"active\":true,\"manifest\":\"Company\"}],\"manifest\":\"ModelCollection\"}",
                b.toJson().toString());
        assertEquals("{\"models\":[{\"name\":\"c\",\"active\":true,\"manifest\":\"Company\"}],"
                + "\"manifest\":\"ModelCollection\"}", c.toJson().toString());
    }

    @Test
    public void testToString() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final ModelCollection<Company> a = new ModelCollection.Builder<Company>().build();
        final ModelCollection<Company> b = new ModelCollection.Builder<>(companyA, companyB).build();
        final ModelCollection<Company> c = new ModelCollection.Builder<>(companyC).build();

        assertEquals("ModelCollection[models=[]]", a.toString());
        assertEquals("ModelCollection[models=[Company[id=Optional.empty,name=a,active=true], "
                + "Company[id=Optional.empty,name=b,active=true]]]", b.toString());
        assertEquals("ModelCollection[models=[Company[id=Optional.empty,name=c,active=true]]]", c.toString());
    }

    @Test
    public void testBuilderAdd() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final ModelCollection<Company> response =
                new ModelCollection.Builder<Company>().add(companyA).add(Arrays.asList(companyB, companyC)).build();

        assertEquals(3, response.getModels().size());
        assertTrue(response.getModels().contains(companyA));
        assertTrue(response.getModels().contains(companyB));
        assertTrue(response.getModels().contains(companyC));
    }

    @Test
    public void testBuilderNoCompanies() {
        assertEquals("ModelCollection[models=[]]", new ModelCollection.Builder().build().toString());
    }

    @Test
    public void testBuilderFromJson() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final ModelCollection<Company> original = new ModelCollection.Builder<>(companyA, companyB, companyC).build();
        final ModelCollection<Company> copy =
                new ModelCollection.Builder<Company>().fromJson(mapping, original.toJson()).build();

        assertEquals(original, copy);
    }

    @Test
    public void testBuilderFromJsonNoModels() {
        // a JsonObject with no "models" element.
        final JsonObject json = new JsonParser().parse("{\"manifest\":\"ModelCollection\"}").getAsJsonObject();

        final ModelCollection<Company> empty = new ModelCollection.Builder<Company>().build();
        final ModelCollection<Company> copy =
                new ModelCollection.Builder<Company>().fromJson(mapping, json).build();

        assertEquals(empty, copy);
    }

    @Test
    public void testBuilderFromJsonNoBuilder() {
        // a JsonObject with manifest unrecognized.
        final JsonObject json = new JsonParser()
                .parse("{\"models\":[{\"name\":\"a\",\"active\":true,\"manifest\":\"Unrecognized\"}],"
                        + "\"manifest\":\"ModelCollection\"}").getAsJsonObject();

        final ModelCollection<Company> empty = new ModelCollection.Builder<Company>().build();
        final ModelCollection<Company> copy =
                new ModelCollection.Builder<Company>().fromJson(mapping, json).build();

        assertEquals(empty, copy);
    }
}
