package mysystem.db.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Perform testing of the {@link DatabaseConfig} enumeration.
 */
public class DatabaseConfigTest {
    @Test
    public void test() {
        // This is only here for 100% coverage.
        assertEquals(DatabaseConfig.DATABASE_USERNAME, DatabaseConfig.valueOf("DATABASE_USERNAME"));
        assertEquals(5, DatabaseConfig.values().length);
    }

    @Test
    public void testGetKey() {
        assertEquals("mysystem.database.username", DatabaseConfig.DATABASE_USERNAME.getKey());
    }
}
