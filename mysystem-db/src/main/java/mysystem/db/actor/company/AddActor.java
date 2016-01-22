package mysystem.db.actor.company;

import com.mysql.jdbc.Statement;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.CircuitBreaker;
import akka.pattern.Patterns;
import mysystem.common.model.Company;
import mysystem.db.model.Add;
import mysystem.db.model.company.CompanyResponse;
import scala.concurrent.Future;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

/**
 * This actor is responsible for adding {@link Company} objects to the configured data source.
 */
public class AddActor extends UntypedActor {
    private final DataSource dataSource;
    private final CircuitBreaker circuitBreaker;

    /**
     * @param actorContext the {@link ActorContext} that will host the actor
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(
            final ActorContext actorContext, final DataSource dataSource, final CircuitBreaker circuitBreaker) {
        final Props props = Props.create(AddActor.class, dataSource, circuitBreaker);
        return Objects.requireNonNull(actorContext).actorOf(props, AddActor.class.getSimpleName());
    }

    /**
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     */
    public AddActor(final DataSource dataSource, final CircuitBreaker circuitBreaker) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.circuitBreaker = Objects.requireNonNull(circuitBreaker);
    }

    protected DataSource getDataSource() {
        return this.dataSource;
    }

    protected CircuitBreaker getCircuitBreaker() {
        return this.circuitBreaker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void onReceive(final Object message) {
        if (message instanceof Add) {
            final Callable<Future<CompanyResponse>> callable = handleAdd((Add<Company>) message);
            final Future<CompanyResponse> future = getCircuitBreaker().callWithCircuitBreaker(callable);
            Patterns.pipe(future, context().dispatcher()).to(sender());
        } else {
            unhandled(message);
        }
    }

    protected Callable<Future<CompanyResponse>> handleAdd(final Add<Company> add) {
        final String sql = "INSERT INTO companies (name, active) VALUES (?, ?)";

        return () -> Futures.future(() -> {
            final CompanyResponse.Builder builder = new CompanyResponse.Builder();

            try (final Connection conn = getDataSource().getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                for (final Company company : add.getModels()) {
                    ps.setString(1, company.getName());
                    ps.setBoolean(2, company.isActive());

                    try (final ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            builder.add(new Company.Builder(company).setId(rs.getInt(1)).build());
                        }
                    }
                }
            }

            return builder.build();
        }, context().dispatcher());
    }
}
