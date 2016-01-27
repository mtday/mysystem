package mysystem.rest.factory;

import java.util.Objects;

import javax.servlet.ServletContext;

/**
 * The default implementation of a factory used to inject objects into the REST resources, retrieving the object from
 * the servlet context.
 */
public abstract class BaseServletContextFactory<T> extends BaseFactory<T> {
    private final ServletContext servletContext;
    private final String attributeName;

    /**
     * @param servletContext the servlet context associated with the request
     * @param attributeName the name of the request attribute from which this factory will retrieve objects
     */
    public BaseServletContextFactory(final ServletContext servletContext, final String attributeName) {
        this.servletContext = Objects.requireNonNull(servletContext);
        this.attributeName = Objects.requireNonNull(attributeName);
    }

    /**
     * @return the servlet context associated with the request
     */
    protected ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * @return the name of the request attribute from which this factory will retrieve objects
     */
    protected String getAttributeName() {
        return this.attributeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public T provide() {
        return (T) getServletContext().getAttribute(getAttributeName());
    }
}
