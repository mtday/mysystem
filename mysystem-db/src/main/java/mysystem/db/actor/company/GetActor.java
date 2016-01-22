package mysystem.db.actor.company;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.pattern.CircuitBreaker;
import akka.pattern.Patterns;
import mysystem.common.model.Company;
import mysystem.db.model.GetAll;
import mysystem.db.model.GetById;
import mysystem.db.model.company.CompanyResponse;
import scala.concurrent.Future;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

/**
 * This actor is responsible for retrieving {@link mysystem.common.model.Company} objects from the configured data
 * source.
 */
public class GetActor extends UntypedActor {
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
        final Props props = Props.create(GetActor.class, dataSource, circuitBreaker);
        return Objects.requireNonNull(actorContext).actorOf(props, GetActor.class.getSimpleName());
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
            final Callable<Future<CompanyResponse>> callable = handleGetById((GetById) message);
            final Future<CompanyResponse> future = getCircuitBreaker().callWithCircuitBreaker(callable);
            Patterns.pipe(future, context().dispatcher()).to(sender());
        } else if (message instanceof GetAll) {
            final Future<CompanyResponse> future = getCircuitBreaker().callWithCircuitBreaker(handleGetAll());
            Patterns.pipe(future, context().dispatcher()).to(sender());
        } else {
            unhandled(message);
        }
    }

    protected Callable<Future<CompanyResponse>> handleGetById(final GetById getById) {
        final String sql = "SELECT id, name, active FROM companies WHERE id = ?";

        return () -> Futures.future(() -> {
            final CompanyResponse.Builder builder = new CompanyResponse.Builder();

            try (final Connection conn = getDataSource().getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql)) {
                for (final Integer id : getById.getIds()) {
                    ps.setInt(1, id);

                    try (final ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            final Company.Builder companyBuilder = new Company.Builder();
                            companyBuilder.setId(rs.getInt("id"));
                            companyBuilder.setName(rs.getString("name"));
                            companyBuilder.setActive(rs.getBoolean("active"));
                            builder.add(companyBuilder.build());
                        }
                    }
                }
            }

            return builder.build();
        }, context().dispatcher());
    }

    protected Callable<Future<CompanyResponse>> handleGetAll() {
        final String sql = "SELECT id, name, active FROM companies";

        return () -> Futures.future(() -> {
            final CompanyResponse.Builder builder = new CompanyResponse.Builder();

            try (final Connection conn = getDataSource().getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql);
                 final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final Company.Builder companyBuilder = new Company.Builder();
                    companyBuilder.setId(rs.getInt("id"));
                    companyBuilder.setName(rs.getString("name"));
                    companyBuilder.setActive(rs.getBoolean("active"));
                    builder.add(companyBuilder.build());
                }
            }

            return builder.build();
        }, context().dispatcher());
    }
}
