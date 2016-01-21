package mysystem.common.config;

/**
 * Defines the interface required of configuration keys.
 */
public interface ConfigKey {
    /**
     * @return the key to use when retrieving the value for this configuration item
     */
    String getKey();
}
