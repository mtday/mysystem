package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorContext;
import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import akka.pattern.CircuitBreaker;
import mysystem.db.actor.company.CompanyManager;
import scala.concurrent.ExecutionContextExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing of the {@link DatabaseActorConfig} class and builder.
 */
public class DatabaseActorConfigTest {
    private Config getConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("class", ConfigValueFactory.fromAnyRef(CompanyManager.class.getName()));
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoClass() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigMissingClass() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("class", ConfigValueFactory.fromAnyRef("missing"));
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoDataType() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("class", ConfigValueFactory.fromAnyRef(CompanyManager.class.getName()));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoMaxFailures() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("class", ConfigValueFactory.fromAnyRef(CompanyManager.class.getName()));
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoCallTimeout() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("class", ConfigValueFactory.fromAnyRef(CompanyManager.class.getName()));
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoResetTimeout() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("class", ConfigValueFactory.fromAnyRef(CompanyManager.class.getName()));
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        return ConfigFactory.parseMap(map);
    }

    @Test
    public void testCompareTo() {
        final DatabaseActorConfig a = new DatabaseActorConfig.Builder("a", getConfig()).build();
        final DatabaseActorConfig b = new DatabaseActorConfig.Builder("b", getConfig()).build();
        final DatabaseActorConfig c = new DatabaseActorConfig.Builder("c", getConfig()).build();

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-2, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(2, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final DatabaseActorConfig a = new DatabaseActorConfig.Builder("a", getConfig()).build();
        final DatabaseActorConfig b = new DatabaseActorConfig.Builder("b", getConfig()).build();
        final DatabaseActorConfig c = new DatabaseActorConfig.Builder("c", getConfig()).build();

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
        final DatabaseActorConfig a = new DatabaseActorConfig.Builder("a", getConfig()).build();
        final DatabaseActorConfig b = new DatabaseActorConfig.Builder("b", getConfig()).build();
        final DatabaseActorConfig c = new DatabaseActorConfig.Builder("c", getConfig()).build();

        assertEquals(-1429830597, a.hashCode());
        assertEquals(-1360486640, b.hashCode());
        assertEquals(-1291142683, c.hashCode());
    }

    @Test
    public void testToString() {
        final DatabaseActorConfig config = new DatabaseActorConfig.Builder("a", getConfig()).build();

        final StringBuilder expected = new StringBuilder();
        expected.append("DatabaseActorConfig[actorName=a,actorClass=mysystem.db.actor.company.CompanyManager,");
        expected.append("dataType=COMPANY,maxFailures=5,callTimeout=10000 milliseconds,");
        expected.append("resetTimeout=60000 milliseconds]");

        assertEquals(expected.toString(), config.toString());
    }

    @Test
    public void testGetCircuitBreaker() {
        final Scheduler scheduler = Mockito.mock(Scheduler.class);

        final ActorSystem actorSystem = Mockito.mock(ActorSystem.class);
        Mockito.when(actorSystem.scheduler()).thenReturn(scheduler);

        final ExecutionContextExecutor dispatcher = Mockito.mock(ExecutionContextExecutor.class);

        final ActorContext actorContext = Mockito.mock(ActorContext.class);
        Mockito.when(actorContext.dispatcher()).thenReturn(dispatcher);
        Mockito.when(actorContext.system()).thenReturn(actorSystem);

        final CircuitBreaker circuitBreaker =
                new DatabaseActorConfig.Builder("a", getConfig()).build().getCircuitBreaker(actorContext);
        assertNotNull(circuitBreaker);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoClass() {
        new DatabaseActorConfig.Builder("a", getConfigNoClass()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMissingClass() {
        new DatabaseActorConfig.Builder("a", getConfigMissingClass()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoDataType() {
        new DatabaseActorConfig.Builder("a", getConfigNoDataType()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoMaxFailures() {
        new DatabaseActorConfig.Builder("a", getConfigNoMaxFailures()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoCallTimeout() {
        new DatabaseActorConfig.Builder("a", getConfigNoCallTimeout()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoResetTimeout() {
        new DatabaseActorConfig.Builder("a", getConfigNoResetTimeout()).build();
    }

    @Test
    public void testBuilderCopy() {
        final DatabaseActorConfig a = new DatabaseActorConfig.Builder("a", getConfig()).build();
        final DatabaseActorConfig b = new DatabaseActorConfig.Builder(a).build();
        assertEquals(a, b);
    }
}
