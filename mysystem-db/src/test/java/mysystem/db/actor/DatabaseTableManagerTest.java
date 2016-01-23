package mysystem.db.actor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.hsqldb.jdbc.JDBCDriver;
import org.junit.Test;
import org.mockito.Mockito;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.pattern.CircuitBreaker;
import akka.testkit.JavaTestKit;
import mysystem.common.model.Company;
import mysystem.db.actor.company.GetActor;
import mysystem.db.config.DatabaseConfig;
import mysystem.db.model.Add;
import mysystem.db.model.DataType;
import mysystem.db.model.DatabaseManagerConfig;
import mysystem.db.model.GetAll;
import mysystem.db.model.GetById;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Perform testing of the {@link DatabaseTableManager} class.
 */
public class DatabaseTableManagerTest {
    private static Config getConfig() {
        return getActorConfig().withFallback(getDatabaseConfig()).withFallback(ConfigFactory.load("test-config"));
    }

    private static Config getActorConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put("data-type", ConfigValueFactory.fromAnyRef(DataType.COMPANY.name()));
        map.put("max-failures", ConfigValueFactory.fromAnyRef(5));
        map.put("call-timeout", ConfigValueFactory.fromAnyRef("10 s"));
        map.put("reset-timeout", ConfigValueFactory.fromAnyRef("60 s"));
        map.put("actors.get-by-id.actor-class", ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put("actors.get-by-id.message-class", ConfigValueFactory.fromAnyRef(GetById.class.getName()));
        map.put("actors.get-all.actor-class", ConfigValueFactory.fromAnyRef(GetActor.class.getName()));
        map.put("actors.get-all.message-class", ConfigValueFactory.fromAnyRef(GetAll.class.getName()));
        return ConfigFactory.parseMap(map);
    }

    private static Config getDatabaseConfig() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(
                DatabaseConfig.DATABASE_DRIVER_CLASS.getKey(),
                ConfigValueFactory.fromAnyRef(JDBCDriver.class.getName()));
        map.put(DatabaseConfig.DATABASE_JDBC_URL.getKey(), ConfigValueFactory.fromAnyRef("jdbc:hsqldb:mem:testdb"));
        map.put(DatabaseConfig.DATABASE_USERNAME.getKey(), ConfigValueFactory.fromAnyRef("SA"));
        map.put(DatabaseConfig.DATABASE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef(""));
        return ConfigFactory.parseMap(map);
    }

    @Test
    public void testReceiveWithAdd() {
        final ActorSystem system = ActorSystem.create("test-add", getConfig());
        new JavaTestKit(system) {{
            final DatabaseManagerConfig managerConfig =
                    new DatabaseManagerConfig.Builder("company", getActorConfig()).build();
            final DataSource dataSource = Mockito.mock(DataSource.class);
            final CircuitBreaker circuitBreaker = Mockito.mock(CircuitBreaker.class);
            final ActorRef cmpmgr =
                    system.actorOf(Props.create(DatabaseTableManager.class, managerConfig, dataSource, circuitBreaker));

            final Company company = new Company.Builder().setName("name").build();
            final Add<Company> add = new Add.Builder<>(DataType.COMPANY, company).build();

            try {
                cmpmgr.tell(add, getRef());

                expectNoMsg(duration("100 ms"));
            } finally {
                cmpmgr.tell(PoisonPill.getInstance(), getRef());
                system.terminate();
            }
        }};
    }
}
