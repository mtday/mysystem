package mysystem.tomcat.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Perform testing of the {@link TomcatConfig} enumeration.
 */
public class TomcatConfigTest {
    @Test
    public void test() {
        // This is only here for 100% coverage.
        assertEquals(TomcatConfig.TOMCAT_PORT_INSECURE, TomcatConfig.valueOf("TOMCAT_PORT_INSECURE"));
        assertTrue(TomcatConfig.values().length > 0);
    }

    @Test
    public void testGetKey() {
        assertEquals("mysystem.tomcat.port.insecure", TomcatConfig.TOMCAT_PORT_INSECURE.getKey());
    }
}
