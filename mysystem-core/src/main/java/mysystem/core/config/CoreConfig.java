package mysystem.core.config;

/**
 *
 */
public enum CoreConfig implements ConfigKey {
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
