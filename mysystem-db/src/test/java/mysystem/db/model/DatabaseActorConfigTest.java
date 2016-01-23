package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;

import mysystem.db.actor.company.GetActor;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing of the {@link DatabaseActorConfig} class and builder.
 */
public class DatabaseActorConfigTest {
    private Config getConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("actor-class", ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put("message-class", ConfigValueFactory.fromAnyRef(GetAll.class.getName()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoActorClass() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("message-class", ConfigValueFactory.fromAnyRef(GetAll.class.getName()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigMissingActorClass() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("actor-class", ConfigValueFactory.fromAnyRef("missing"));
        map.put("message-class", ConfigValueFactory.fromAnyRef(GetAll.class.getName()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoMessageClass() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("actor-class", ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigMissingMessageClass() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("actor-class", ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put("message-class", ConfigValueFactory.fromAnyRef("missing"));
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

        assertEquals(-198566735, a.hashCode());
        assertEquals(-198565366, b.hashCode());
        assertEquals(-198563997, c.hashCode());
    }

    @Test
    public void testToString() {
        final DatabaseActorConfig config = new DatabaseActorConfig.Builder("a", getConfig()).build();

        final StringBuilder expected = new StringBuilder();
        expected.append("DatabaseActorConfig[actorName=a,actorClass=mysystem.db.actor.company.GetActor,");
        expected.append("messageClass=mysystem.db.model.GetAll]");

        assertEquals(expected.toString(), config.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoActorClass() {
        new DatabaseActorConfig.Builder("a", getConfigNoActorClass()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMissingActorClass() {
        new DatabaseActorConfig.Builder("a", getConfigMissingActorClass()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNoMessageClass() {
        new DatabaseActorConfig.Builder("a", getConfigNoMessageClass()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMissingMessageClass() {
        new DatabaseActorConfig.Builder("a", getConfigMissingMessageClass()).build();
    }

    @Test
    public void testBuilderCopy() {
        final DatabaseActorConfig a = new DatabaseActorConfig.Builder("a", getConfig()).build();
        final DatabaseActorConfig b = new DatabaseActorConfig.Builder(a).build();
        assertEquals(a, b);
    }
}
