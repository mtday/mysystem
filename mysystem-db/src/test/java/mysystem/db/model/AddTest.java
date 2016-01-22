package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import mysystem.common.model.Company;

import java.util.Arrays;

/**
 * Perform testing on the {@link Add} class.
 */
public class AddTest {
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

        assertEquals("Add[dataType=COMPANY,models=[Company[id=Optional.empty,name=a,active=true]]]", add.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoModels() {
        new Add.Builder(DataType.COMPANY).build();
    }
}
