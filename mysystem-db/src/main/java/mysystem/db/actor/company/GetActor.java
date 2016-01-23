package mysystem.db.actor.company;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.CircuitBreaker;
import akka.pattern.Patterns;
import mysystem.common.model.Company;
import mysystem.db.model.GetAll;
import mysystem.db.model.GetById;
import mysystem.db.model.ModelCollection;
import scala.concurrent.Future;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

/**
 * This actor is responsible for retrieving {@link Company} objects from the configured data source.
 */
public class GetActor extends UntypedActor {
    private final DataSource dataSource;
    private final CircuitBreaker circuitBreaker;

    /**
     * @param actorRefFactory the {@link ActorRefFactory} that will be used to create actor references
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     * @return an {@link ActorRef} for the created actor
     */
    public static ActorRef create(
            final ActorRefFactory actorRefFactory, final DataSource dataSource, final CircuitBreaker circuitBreaker) {
        final Props props = Props.create(GetActor.class, dataSource, circuitBreaker);
        return Objects.requireNonNull(actorRefFactory).actorOf(props, GetActor.class.getSimpleName());
    }

    /**
     * @param dataSource the {@link DataSource} used to manage database connections
     * @param circuitBreaker the {@link CircuitBreaker} used to manage push-back when the database gets overloaded
     */
    public GetActor(final DataSource dataSource, final CircuitBreaker circuitBreaker) {
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
    public void onReceive(final Object message) {
        if (message instanceof GetById) {
            final Callable<Future<ModelCollection>> callable = handleGetById((GetById) message);
            final Future<ModelCollection> future = getCircuitBreaker().callWithCircuitBreaker(callable);
            Patterns.pipe(future, context().dispatcher()).to(sender());
        } else if (message instanceof GetAll) {
            final Callable<Future<ModelCollection>> callable = handleGetAll((GetAll) message);
            final Future<ModelCollection> future = getCircuitBreaker().callWithCircuitBreaker(callable);
            Patterns.pipe(future, context().dispatcher()).to(sender());
        } else {
            unhandled(message);
        }
    }

    protected String getSql(final GetById getById) {
        final List<String> parts = new LinkedList<>();
        parts.add("SELECT id, name, active FROM companies WHERE");
        parts.add("id = ?");
        if (getById.getActive().isPresent()) {
            parts.add("AND active = ?");
        }
        return String.join(" ", parts);
    }

    protected String getSql(final GetAll getAll) {
        final List<String> parts = new LinkedList<>();
        parts.add("SELECT id, name, active FROM companies");
        if (getAll.getActive().isPresent()) {
            parts.add("WHERE active = ?");
        }
        return String.join(" ", parts);
    }

    protected void setStatementParameters(final PreparedStatement ps, final GetById getById, final Integer id)
            throws SQLException {
        ps.setInt(1, id);
        if (getById.getActive().isPresent()) {
            ps.setBoolean(2, getById.getActive().get());
        }
    }

    protected void setStatementParameters(final PreparedStatement ps, final GetAll getAll) throws SQLException {
        if (getAll.getActive().isPresent()) {
            ps.setBoolean(1, getAll.getActive().get());
        }
    }

    protected Company getCompany(final ResultSet resultSet) throws SQLException {
        final Company.Builder companyBuilder = new Company.Builder();
        companyBuilder.setId(resultSet.getInt("id"));
        companyBuilder.setName(resultSet.getString("name"));
        companyBuilder.setActive(resultSet.getBoolean("active"));
        return companyBuilder.build();
    }

    protected void populateCompanyResponse(final ModelCollection.Builder<Company> builder, final ResultSet resultSet)
            throws SQLException {
        while (resultSet.next()) {
            builder.add(getCompany(resultSet));
        }
    }

    protected Callable<Future<ModelCollection>> handleGetById(final GetById getById) {
        return () -> Futures.future(() -> {
            final ModelCollection.Builder<Company> builder = new ModelCollection.Builder<>();

            try (final Connection conn = getDataSource().getConnection();
                 final PreparedStatement ps = conn.prepareStatement(getSql(getById))) {
                for (final Integer id : getById.getIds()) {
                    setStatementParameters(ps, getById, id);

                    try (final ResultSet rs = ps.executeQuery()) {
                        populateCompanyResponse(builder, rs);
                    }
                }
            }

            return builder.build();
        }, context().dispatcher());
    }

    protected Callable<Future<ModelCollection>> handleGetAll(final GetAll getAll) {
        return () -> Futures.future(() -> {
            final ModelCollection.Builder<Company> builder = new ModelCollection.Builder<>();

            try (final Connection conn = getDataSource().getConnection();
                 final PreparedStatement ps = conn.prepareStatement(getSql(getAll))) {
                setStatementParameters(ps, getAll);
                try (final ResultSet rs = ps.executeQuery()) {
                    populateCompanyResponse(builder, rs);
                }
            }

            return builder.build();
        }, context().dispatcher());
    }
}
