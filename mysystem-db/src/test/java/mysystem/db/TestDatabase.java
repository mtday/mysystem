package mysystem.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.hsqldb.jdbc.JDBCDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * Used to load data into an HSQLDB instance for testing.
 */
public class TestDatabase {
    private final DataSource dataSource;

    /**
     * Default constructor.
     */
    public TestDatabase() {
        this.dataSource = createDataSource();
    }

    /**
     * @return the {@link DataSource} used to communicate with the HSQLDB instance
     */
    private DataSource createDataSource() {
        final HikariConfig dbConfig = new HikariConfig();
        dbConfig.setAutoCommit(true);
        dbConfig.setDriverClassName(JDBCDriver.class.getName());
        dbConfig.setJdbcUrl("jdbc:hsqldb:mem:testdb");
        dbConfig.setUsername("SA");
        dbConfig.setPassword("");
        return new HikariDataSource(dbConfig);
    }

    /**
     * @return the {@link DataSource} used to communicate with the HSQLDB instance
     */
    public DataSource getDataSource() {
        return this.dataSource;
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
