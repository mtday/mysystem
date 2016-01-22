package mysystem.common.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link CommonConfig} enumeration.
 */
public class CommonConfigTest {
    @Test
    public void test() {
        // This is only here for 100% coverage.
        assertEquals(CommonConfig.VERSION, CommonConfig.valueOf("VERSION"));
        assertTrue(CommonConfig.values().length > 0);
    }

    @Test
    public void testGetKey() {
        assertEquals("mysystem.version", CommonConfig.VERSION.getKey());
        assertEquals("mysystem.actor.system.name", CommonConfig.ACTOR_SYSTEM_NAME.getKey());
    }
}
