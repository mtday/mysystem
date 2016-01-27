package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link DataType} enumeration.
 */
public class DataTypeTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertEquals(DataType.COMPANY, DataType.valueOf("COMPANY"));
        assertTrue(DataType.values().length > 0);
    }
}
