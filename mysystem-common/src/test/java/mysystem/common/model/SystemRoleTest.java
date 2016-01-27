package mysystem.common.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link SystemRole} enumeration.
 */
public class SystemRoleTest {
    @Test
    public void test() {
        // Only here for 100% coverage.
        assertEquals(SystemRole.SYSTEM, SystemRole.valueOf("SYSTEM"));
        assertTrue(SystemRole.values().length > 0);
    }
}
