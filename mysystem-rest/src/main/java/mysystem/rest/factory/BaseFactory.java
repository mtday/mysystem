package mysystem.rest.factory;

import org.glassfish.hk2.api.Factory;

/**
 * THe default implementation of a factory used to inject objects into the REST resources.
 */
public abstract class BaseFactory<T> implements Factory<T> {
    /**
     * {@inheritDoc}
     */
    @Override
    public T provide() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(final T instance) {
        // Nothing to do.
    }
}
