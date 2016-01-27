package mysystem.rest;

import com.google.common.reflect.ClassPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mysystem.rest.factory.ActorSystemFactory;
import mysystem.rest.factory.ConfigFactory;

import java.io.IOException;

/**
 * The REST application class used to define the available resources.
 */
public class Application extends ResourceConfig {
    private final static Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * Default constructor.
     */
    public Application() {
        LOG.info("Loading core application resources");

        // Our own register method is used instead of packages since packages has trouble finding the resources when
        // Tomcat is run as an embedded app.
        registerPackage(getClass().getPackage().getName());

        register(ConfigFactory.getBinder());
        register(ActorSystemFactory.getBinder());
    }

    public void registerPackage(final String packageName) {
        try {
            final ClassPath classPath = ClassPath.from(getClass().getClassLoader());
            classPath.getTopLevelClassesRecursive(packageName).stream().map(ClassPath.ClassInfo::getName).sorted()
                    .forEach(className -> {
                        try {
                            register(Class.forName(className));
                        } catch (final ClassNotFoundException notFound) {
                            LOG.error("Failed to load class.", notFound);
                        }
                    });
        } catch (final IOException classpathIssue) {
            LOG.error("Failed to retrieve resources from class path.", classpathIssue);
        }
    }
}
