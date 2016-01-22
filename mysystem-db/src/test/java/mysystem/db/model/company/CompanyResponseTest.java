package mysystem.db.model.company;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import mysystem.common.model.Company;

import java.util.Arrays;

/**
 * Perform testing on the {@link CompanyResponse} class.
 */
public class CompanyResponseTest {
    @Test
    public void testCompareTo() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final CompanyResponse a = new CompanyResponse.Builder(companyA).build();
        final CompanyResponse b = new CompanyResponse.Builder(companyA, companyB).build();
        final CompanyResponse c = new CompanyResponse.Builder(companyC).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-2, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-2, b.compareTo(c));
        assertEquals(2, c.compareTo(a));
        assertEquals(2, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final CompanyResponse a = new CompanyResponse.Builder(companyA).build();
        final CompanyResponse b = new CompanyResponse.Builder(companyA, companyB).build();
        final CompanyResponse c = new CompanyResponse.Builder(companyC).build();

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

        final CompanyResponse a = new CompanyResponse.Builder(companyA).build();
        final CompanyResponse b = new CompanyResponse.Builder(companyA, companyB).build();
        final CompanyResponse c = new CompanyResponse.Builder(companyC).build();

        assertEquals(2034684798, a.hashCode());
        assertEquals(-225597663, b.hashCode());
        assertEquals(2034684872, c.hashCode());
    }

    @Test
    public void testToString() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final CompanyResponse a = new CompanyResponse.Builder(companyA).build();
        final CompanyResponse b = new CompanyResponse.Builder(companyA, companyB).build();
        final CompanyResponse c = new CompanyResponse.Builder(companyC).build();

        assertEquals("CompanyResponse[companies=[Company[id=Optional.absent(),name=a,active=true]]]", a.toString());
        assertEquals("CompanyResponse[companies=[Company[id=Optional.absent(),name=a,active=true], " +
                "Company[id=Optional.absent(),name=b,active=true]]]", b.toString());
        assertEquals("CompanyResponse[companies=[Company[id=Optional.absent(),name=c,active=true]]]", c.toString());
    }

    @Test
    public void testBuilderAdd() {
        final Company companyA = new Company.Builder().setName("a").build();
        final Company companyB = new Company.Builder().setName("b").build();
        final Company companyC = new Company.Builder().setName("c").build();

        final CompanyResponse response =
                new CompanyResponse.Builder().add(companyA).add(Arrays.asList(companyB, companyC)).build();

        assertEquals(3, response.getCompanies().size());
        assertTrue(response.getCompanies().contains(companyA));
        assertTrue(response.getCompanies().contains(companyB));
        assertTrue(response.getCompanies().contains(companyC));
    }

    @Test
    public void testBuilderNoCompanies() {
        assertEquals("CompanyResponse[companies=[]]", new CompanyResponse.Builder().build().toString());
    }
}
