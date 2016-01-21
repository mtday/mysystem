package mysystem.shell.config;

import mysystem.common.config.CommonConfig;
import mysystem.common.config.ConfigKey;

/**
 * Provides configuration keys relevant to the shell.
 */
public enum ShellConfig implements ConfigKey {
    /**
     * The configuration location where the shell commands are defined.
     */
    SHELL_COMMANDS,

    ;

    /**
     * @return the key to use when retrieving the value for this configuration item
     */
    public String getKey() {
        return String.join(".", CommonConfig.CONFIG_PREFIX, name().toLowerCase().replace('_', '.'));
    }
}
