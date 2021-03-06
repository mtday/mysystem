package mysystem.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorContext;
import akka.actor.ActorSystem;
import akka.actor.Scheduler;
import akka.pattern.CircuitBreaker;
import mysystem.common.serialization.ManifestMapping;
import mysystem.db.actor.company.GetActor;
import scala.concurrent.ExecutionContextExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing of the {@link DatabaseManagerConfig} class and builder.
 */
public class DatabaseManagerConfigTest {
    private final ManifestMapping mapping = new ManifestMapping();

    private ConfigObject getActorConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("get-all.actor-class", ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put("get-all.message-class", ConfigValueFactory.fromAnyRef(GetAll.class.getName()));
        map.put("get-by-id.actor-class", ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put("get-by-id.message-class", ConfigValueFactory.fromAnyRef(GetAll.class.getName()));
        map.put("invalid", ConfigValueFactory.fromAnyRef("whatever"));
        return ConfigFactory.parseMap(map).root();
    }

    private Config getConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        map.put("actors", ConfigValueFactory.fromAnyRef(getActorConfig()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoDataType() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        map.put("actors", ConfigValueFactory.fromAnyRef(getActorConfig()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoMaxFailures() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        map.put("actors", ConfigValueFactory.fromAnyRef(getActorConfig()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoCallTimeout() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        map.put("actors", ConfigValueFactory.fromAnyRef(getActorConfig()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoResetTimeout() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("actors", ConfigValueFactory.fromAnyRef(getActorConfig()));
        return ConfigFactory.parseMap(map);
    }

    private Config getConfigNoActors() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("1 m"));
        return ConfigFactory.parseMap(map);
    }

    @Test
    public void testCompareTo() {
        final DatabaseManagerConfig a = new DatabaseManagerConfig.Builder("a", getConfig()).build();
        final DatabaseManagerConfig b = new DatabaseManagerConfig.Builder("b", getConfig()).build();
        final DatabaseManagerConfig c = new DatabaseManagerConfig.Builder("c", getConfig()).build();

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
        final DatabaseManagerConfig a = new DatabaseManagerConfig.Builder("a", getConfig()).build();
        final DatabaseManagerConfig b = new DatabaseManagerConfig.Builder("b", getConfig()).build();
        final DatabaseManagerConfig c = new DatabaseManagerConfig.Builder("c", getConfig()).build();

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
        final DatabaseManagerConfig a = new DatabaseManagerConfig.Builder("a", getConfig()).build();
        final DatabaseManagerConfig b = new DatabaseManagerConfig.Builder("b", getConfig()).build();
        final DatabaseManagerConfig c = new DatabaseManagerConfig.Builder("c", getConfig()).build();

        assertEquals(-1718487040, a.hashCode());
        assertEquals(-1649143083, b.hashCode());
        assertEquals(-1579799126, c.hashCode());
    }

    @Test
    public void testToJson() {
        final DatabaseManagerConfig config = new DatabaseManagerConfig.Builder("a", getConfig()).build();

        final StringBuilder expected = new StringBuilder();
        expected.append("{\"actorName\":\"a\",\"dataType\":\"COMPANY\",\"maxFailures\":5,\"callTimeout\":10000,");
        expected.append("\"resetTimeout\":60000,\"actorConfigs\":[{\"actorName\":\"get-all\",\"actorClass\":");
        expected.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        expected.append("\"manifest\":\"DatabaseActorConfig\"},{\"actorName\":\"get-by-id\",\"actorClass\":");
        expected.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        expected.append("\"manifest\":\"DatabaseActorConfig\"}],\"manifest\":\"DatabaseManagerConfig\"}");

        assertEquals(expected.toString(), config.toJson().toString());
    }

    @Test
    public void testToString() {
        final DatabaseManagerConfig config = new DatabaseManagerConfig.Builder("a", getConfig()).build();

        final StringBuilder expected = new StringBuilder();
        expected.append("DatabaseManagerConfig[actorName=a,dataType=COMPANY,maxFailures=5,callTimeout=10000 ");
        expected.append("milliseconds,resetTimeout=60000 milliseconds,actorConfigs=[DatabaseActorConfig[actorName=");
        expected.append("get-all,actorClass=mysystem.db.actor.company.GetActor,messageClass=");
        expected.append("mysystem.db.model.GetAll], DatabaseActorConfig[actorName=get-by-id,actorClass=");
        expected.append("mysystem.db.actor.company.GetActor,messageClass=mysystem.db.model.GetAll]]]");

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
                new DatabaseManagerConfig.Builder("a", getConfig()).build().getCircuitBreaker(actorContext);
        assertNotNull(circuitBreaker);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoDataType() {
        new DatabaseManagerConfig.Builder("a", getConfigNoDataType()).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderNegativeMaxFailures() {
        new DatabaseManagerConfig.Builder("a", getConfigNoMaxFailures()).setMaxFailures(-1).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoMaxFailures() {
        new DatabaseManagerConfig.Builder("a", getConfigNoMaxFailures()).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoCallTimeout() {
        new DatabaseManagerConfig.Builder("a", getConfigNoCallTimeout()).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoResetTimeout() {
        new DatabaseManagerConfig.Builder("a", getConfigNoResetTimeout()).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderNoActors() {
        new DatabaseManagerConfig.Builder("a", getConfigNoActors()).build();
    }

    @Test
    public void testBuilderCopy() {
        final DatabaseManagerConfig a = new DatabaseManagerConfig.Builder("a", getConfig()).build();
        final DatabaseManagerConfig b = new DatabaseManagerConfig.Builder(a).build();
        assertEquals(a, b);
    }

    @Test
    public void testBuilderFromJson() {
        final DatabaseManagerConfig original = new DatabaseManagerConfig.Builder("a", getConfig()).build();
        final DatabaseManagerConfig copy =
                new DatabaseManagerConfig.Builder().fromJson(mapping, original.toJson()).build();

        assertEquals(original, copy);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoActorName() {
        final StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{\"dataType\":\"COMPANY\",\"maxFailures\":5,\"callTimeout\":10000,");
        jsonStr.append("\"resetTimeout\":60000,\"actorConfigs\":[{\"actorName\":\"get-all\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"},{\"actorName\":\"get-by-id\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"}],\"manifest\":\"DatabaseManagerConfig\"}");

        final JsonObject json = new JsonParser().parse(jsonStr.toString()).getAsJsonObject();
        new DatabaseManagerConfig.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoDataType() {
        final StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{\"actorName\":\"a\",\"maxFailures\":5,\"callTimeout\":10000,");
        jsonStr.append("\"resetTimeout\":60000,\"actorConfigs\":[{\"actorName\":\"get-all\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"},{\"actorName\":\"get-by-id\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"}],\"manifest\":\"DatabaseManagerConfig\"}");

        final JsonObject json = new JsonParser().parse(jsonStr.toString()).getAsJsonObject();
        new DatabaseManagerConfig.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoMaxFailures() {
        final StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{\"actorName\":\"a\",\"dataType\":\"COMPANY\",\"callTimeout\":10000,");
        jsonStr.append("\"resetTimeout\":60000,\"actorConfigs\":[{\"actorName\":\"get-all\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"},{\"actorName\":\"get-by-id\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"}],\"manifest\":\"DatabaseManagerConfig\"}");

        final JsonObject json = new JsonParser().parse(jsonStr.toString()).getAsJsonObject();
        new DatabaseManagerConfig.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoCallTimeout() {
        final StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{\"actorName\":\"a\",\"dataType\":\"COMPANY\",\"maxFailures\":5,");
        jsonStr.append("\"resetTimeout\":60000,\"actorConfigs\":[{\"actorName\":\"get-all\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"},{\"actorName\":\"get-by-id\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"}],\"manifest\":\"DatabaseManagerConfig\"}");

        final JsonObject json = new JsonParser().parse(jsonStr.toString()).getAsJsonObject();
        new DatabaseManagerConfig.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoResetTimeout() {
        final StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{\"actorName\":\"a\",\"dataType\":\"COMPANY\",\"maxFailures\":5,\"callTimeout\":10000,");
        jsonStr.append("\"actorConfigs\":[{\"actorName\":\"get-all\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"},{\"actorName\":\"get-by-id\",\"actorClass\":");
        jsonStr.append("\"mysystem.db.actor.company.GetActor\",\"messageClass\":\"mysystem.db.model.GetAll\",");
        jsonStr.append("\"manifest\":\"DatabaseActorConfig\"}],\"manifest\":\"DatabaseManagerConfig\"}");

        final JsonObject json = new JsonParser().parse(jsonStr.toString()).getAsJsonObject();
        new DatabaseManagerConfig.Builder().fromJson(mapping, json).build();
    }

    @Test(expected = IllegalStateException.class)
    public void testBuilderFromJsonNoActorConfigs() {
        final StringBuilder jsonStr = new StringBuilder();
        jsonStr.append("{\"actorName\":\"a\",\"dataType\":\"COMPANY\",\"maxFailures\":5,\"callTimeout\":10000,");
        jsonStr.append("\"resetTimeout\":60000,\"manifest\":\"DatabaseManagerConfig\"}");

        final JsonObject json = new JsonParser().parse(jsonStr.toString()).getAsJsonObject();
        new DatabaseManagerConfig.Builder().fromJson(mapping, json).build();
    }
}
