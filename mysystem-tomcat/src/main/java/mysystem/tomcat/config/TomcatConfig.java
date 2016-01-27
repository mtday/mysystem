package mysystem.tomcat.config;

import mysystem.common.config.CommonConfig;
import mysystem.common.config.ConfigKey;

/**
 * An enumeration that defines the keys to use when accessing Tomcat configuration values in the system config.
 */
public enum TomcatConfig implements ConfigKey {
    /**
     * Whether the tomcat server should run in development mode.
     */
    TOMCAT_DEVELOPMENT_MODE,

    /**
     * The location where web application files should be found.
     */
    TOMCAT_WEBAPP_DIR,

    /**
     * Whether the tomcat server should run in insecure mode.
     */
    TOMCAT_INSECURE_MODE,

    /**
     * The insecure (http) port on which the web server will listen.
     */
    TOMCAT_PORT_INSECURE,

    /**
     * The secure (https) port on which the web server will listen.
     */
    TOMCAT_PORT_SECURE,

    /**
     * The server host name published from the web server.
     */
    TOMCAT_HOSTNAME,

    /**
     * The name of the certificate alias in the key store.
     */
    TOMCAT_SSL_KEY_ALIAS,

    /**
     * The key store file containing the server certificate.
     */
    TOMCAT_SSL_KEYSTORE_FILE,

    /**
     * The password to use when accessing the key store.
     */
    TOMCAT_SSL_KEYSTORE_PASS,

    ;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
        return String.join(".", CommonConfig.CONFIG_PREFIX, name().toLowerCase().replace('_', '.'));
    }
}
