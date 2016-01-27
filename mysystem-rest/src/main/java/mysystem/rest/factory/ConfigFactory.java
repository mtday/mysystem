package mysystem.rest.factory;

import com.typesafe.config.Config;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * Provides access to {@link Config} objects within the REST resource classes.
 */
public class ConfigFactory extends BaseServletContextFactory<Config> {
    /** The property to use when retrieving the configuration. */
    public final static String CONFIG_FACTORY_CONFIG = ConfigFactory.class.getCanonicalName() + ".config";

    /**
     * @param servletContext the servlet context from which the system configuration will be retrieved
     */
    public ConfigFactory(final @Context ServletContext servletContext) {
        super(servletContext, CONFIG_FACTORY_CONFIG);
    }

    /**
     * @return a binder that can register this factory
     */
    public static AbstractBinder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(ConfigFactory.class).to(Config.class);
            }
        };
    }
}
