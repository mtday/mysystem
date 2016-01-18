package mysystem.shell.config;

import mysystem.core.config.ConfigKey;
import mysystem.core.config.CoreConfig;

/**
 *
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
        return String.join(".", CoreConfig.CONFIG_PREFIX, name().toLowerCase().replace('_', '.'));
    }
}
