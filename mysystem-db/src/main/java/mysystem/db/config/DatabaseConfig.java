package mysystem.db.config;

import mysystem.common.config.CommonConfig;
import mysystem.common.config.ConfigKey;

/**
 * Defines configuration keys relevant to retrieving database configuration options.
 */
public enum DatabaseConfig implements ConfigKey {
    /**
     * The top-level configuration element where the database actors are defined.
     */
    DATABASE_ACTORS,

    /**
     * The configuration specifying the JDBC driver class.
     */
    DATABASE_DRIVER_CLASS,

    /**
     * The configuration specifying the JDBC connection user name.
     */
    DATABASE_USERNAME,

    /**
     * The configuration specifying the JDBC connection password.
     */
    DATABASE_PASSWORD,

    /**
     * The configuration specifying the JDBC connection URL.
     */
    DATABASE_JDBC_URL,

    ;

    /**
     * @return the key to use when retrieving the value for this configuration item
     */
    public String getKey() {
        return String.join(".", CommonConfig.CONFIG_PREFIX, name().toLowerCase().replace('_', '.'));
    }
}
