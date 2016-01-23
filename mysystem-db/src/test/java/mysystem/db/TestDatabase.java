package mysystem.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.hamcrest.Matcher;
import org.hsqldb.jdbc.JDBCDriver;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Used to load data into an HSQLDB instance for testing.
 */
public class TestDatabase {
    private final DataSource dataSource;

    /**
     * @param databaseName the name of the test database to create
     */
    public TestDatabase(final String databaseName) {
        this.dataSource = createDataSource(databaseName);
    }

    /**
     * @param databaseName the name of the test database to create
     * @return the {@link DataSource} used to communicate with the HSQLDB instance
     */
    private DataSource createDataSource(final String databaseName) {
        final HikariConfig dbConfig = new HikariConfig() {
            @Override
            public void validate() {
                // Suppressing the logging of the configuration.
            }
        };

        dbConfig.setPoolName("hikari-testdb-connection-pool");
        dbConfig.setAutoCommit(true);
        dbConfig.setDriverClassName(JDBCDriver.class.getName());
        dbConfig.setJdbcUrl("jdbc:hsqldb:mem:" + databaseName);
        dbConfig.setUsername("SA");
        dbConfig.setPassword("");
        dbConfig.setMaximumPoolSize(1); // since it is only used for testing
        return new HikariDataSource(dbConfig);
    }

    /**
     * @return the {@link DataSource} used to communicate with the HSQLDB instance
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    public static DataSource getMockDataSourceGetConnectionException() throws SQLException {
        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenThrow(new SQLException("dataSource.getConnection failed"));
        return dataSource;
    }

    public static DataSource getMockDataSourcePrepareStatementException() throws SQLException {
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString()))
                .thenThrow(new SQLException("connection.prepareStatement failed"));
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Matchers.eq(Statement.RETURN_GENERATED_KEYS)))
                .thenThrow(new SQLException("connection.prepareStatement failed"));
        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }

    public static DataSource getMockDataSourceResultSetException() throws SQLException {
        final ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenThrow(new SQLException("resultSet.next failed"));
        final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        Mockito.when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Matchers.eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }

    public static DataSource getMockDataSourceConnectionCloseException() throws SQLException {
        final ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(false);
        final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        Mockito.when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Matchers.eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        Mockito.doThrow(new SQLException("connection.close failed")).when(connection).close();
        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }

    public static DataSource getMockDataSourcePreparedStatementCloseException() throws SQLException {
        final ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(false);
        final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        Mockito.when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        Mockito.doThrow(new SQLException("preparedStatement.close failed")).when(preparedStatement).close();
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Matchers.eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }

    public static DataSource getMockDataSourceResultSetCloseException() throws SQLException {
        final ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(false);
        Mockito.doThrow(new SQLException("resultSet.close failed")).when(resultSet).close();
        final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        Mockito.when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        final Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(preparedStatement);
        Mockito.when(connection.prepareStatement(Mockito.anyString(), Matchers.eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        final DataSource dataSource = Mockito.mock(DataSource.class);
        Mockito.when(dataSource.getConnection()).thenReturn(connection);
        return dataSource;
    }

    /**
     * @param fileOrResource the file path or class path resource of the SQL schema to load
     * @throws IOException if there is a problem loading the schema from the configuration file
     * @throws SQLException if there is a problem loading the schema into the database
     */
    public void load(final String fileOrResource) throws IOException, SQLException {
        StringBuilder sql;
        final File schemaFile = new File(fileOrResource);
        if (!schemaFile.exists()) {
            final URL schemaUrl = getClass().getClassLoader().getResource(fileOrResource);
            if (schemaUrl != null) {
                sql = getSqlFromURL(schemaUrl);
            } else {
                throw new IOException("Failed to find database schema: " + fileOrResource);
            }
        } else {
            sql = getSqlFromFile(schemaFile);
        }

        try (final Connection conn = getDataSource().getConnection();
             final Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
        }
    }

    protected static StringBuilder getSqlFromFile(final File schema) throws IOException {
        final StringBuilder sql = new StringBuilder();
        try (final FileReader fr = new FileReader(schema); final BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                sql.append(line);
                sql.append("\n");
            }
        }
        return sql;
    }

    protected static StringBuilder getSqlFromURL(final URL schema) throws IOException {
        final StringBuilder sql = new StringBuilder();
        try (final InputStream is = schema.openStream();
             final InputStreamReader isr = new InputStreamReader(is);
             final BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                sql.append(line);
                sql.append("\n");
            }
        }
        return sql;
    }
}
