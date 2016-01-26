package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;

import mysystem.common.serialization.ManifestMapping;
import mysystem.db.actor.company.GetActor;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing of the {@link DatabaseActorConfig} class and builder.
 */
public class DatabaseActorConfigTest {
    private final ManifestMapping mapping = new ManifestMapping();

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
    public void testToJson() {
        final DatabaseActorConfig config = new DatabaseActorConfig.Builder("a", getConfig()).build();

        assertEquals(
                "{\"actorName\":\"a\",\"actorClass\":\"mysystem.db.actor.company.GetActor\","
                        + "\"messageClass\":\"mysystem.db.model.GetAll\",\"manifest\":\"DatabaseActorConfig\"}",
                config.toJson().toString());
    }

    @Test
    public void testToString() {
        final DatabaseActorConfig config = new DatabaseActorConfig.Builder("a", getConfig()).build();

        assertEquals(
                "DatabaseActorConfig[actorName=a,actorClass=mysystem.db.actor.company.GetActor,messageClass=mysystem"
                        + ".db.model.GetAll]", config.toString());
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

    @Test
    public void testBuilderFromJson() {
        final DatabaseActorConfig original = new DatabaseActorConfig.Builder("a", getConfig()).build();
        final DatabaseActorConfig copy = new DatabaseActorConfig.Builder().fromJson(mapping, original.toJson()).build();
        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoActorName() {
        final String jsonStr = "{\"actorClass\":\"mysystem.db.actor.company.GetActor\","
                + "\"messageClass\":\"mysystem.db.model.GetAll\",\"manifest\":\"DatabaseActorConfig\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new DatabaseActorConfig.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoActorClass() {
        final String jsonStr =
                "{\"actorName\":\"a\",\"messageClass\":\"mysystem.db.model.GetAll\","
                        + "\"manifest\":\"DatabaseActorConfig\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new DatabaseActorConfig.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoMessageClass() {
        final String jsonStr =
                "{\"actorName\":\"a\",\"actorClass\":\"mysystem.db.actor.company.GetActor\","
                        + "\"manifest\":\"DatabaseActorConfig\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new DatabaseActorConfig.Builder().fromJson(mapping, json).build();
    }
}
