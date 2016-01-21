package mysystem.common.config;

/**
 * An enumeration that defines the keys to use when accessing common configuration values in the system config.
 */
public enum CommonConfig implements ConfigKey {
    /**
     * The system version.
     */
    VERSION,

    /**
     * The system name to use when creating the Akka actor system.
     */
    ACTOR_SYSTEM_NAME,

    ;

    public final static String CONFIG_PREFIX = "mysystem";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return String.join(".", CONFIG_PREFIX, name().toLowerCase().replace('_', '.'));
    }
}
