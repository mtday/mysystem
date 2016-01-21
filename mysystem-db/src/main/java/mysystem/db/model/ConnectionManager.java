package mysystem.db.model;

import javax.sql.DataSource;

/**
 * Used to manage database connections.
 */
public class ConnectionManager {
    private final DataSource dataSource;

    public ConnectionManager() {
        this.dataSource = new com.zaxxer.hikari.HikariDataSource();
    }
}
