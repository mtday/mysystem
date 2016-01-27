package mysystem.rest.factory;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import akka.actor.ActorSystem;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 * Provides access to the {@link ActorSystem} object within the REST resource classes.
 */
public class ActorSystemFactory extends BaseServletContextFactory<ActorSystem> {
    /** The property to use when retrieving the configuration. */
    public final static String ACTOR_SYSTEM_FACTORY_CONFIG = ActorSystemFactory.class.getCanonicalName() + ".config";

    /**
     * @param servletContext the servlet context from which the actor system will be retrieved
     */
    public ActorSystemFactory(final @Context ServletContext servletContext) {
        super(servletContext, ACTOR_SYSTEM_FACTORY_CONFIG);
    }

    /**
     * @return a binder that can register this factory
     */
    public static AbstractBinder getBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(ActorSystemFactory.class).to(ActorSystem.class);
            }
        };
    }
}
