package mysystem.tomcat.server;

import com.typesafe.config.Config;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.jasper.servlet.JspServlet;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;

import akka.actor.ActorSystem;
import mysystem.rest.Application;
import mysystem.rest.factory.ActorSystemFactory;
import mysystem.rest.factory.ConfigFactory;
import mysystem.tomcat.config.TomcatConfig;

import java.util.Arrays;
import java.util.Objects;

/**
 * Provides an embedded Tomcat server implementation.
 */
public class TomcatServer {
    private final Tomcat tomcat;

    /**
     * Initializes the embedded Tomcat server.
     *
     * @param actorSystem the {@link ActorSystem} hosting this tomcat server
     * @param config the system configuration
     */
    public TomcatServer(final ActorSystem actorSystem, final Config config) {
        Objects.requireNonNull(config);

        // The base directory where the web application will operate.
        final String baseDir = config.getBoolean(TomcatConfig.TOMCAT_DEVELOPMENT_MODE.getKey()) ? "target" : ".";

        this.tomcat = new Tomcat();
        this.tomcat.setBaseDir(baseDir);
        this.tomcat.getHost().setAppBase(".");

        final Service service = this.tomcat.getService();
        final Connector insecureConnector = getInsecureConnector(config);
        this.tomcat.setConnector(insecureConnector);
        if (!config.getBoolean(TomcatConfig.TOMCAT_INSECURE_MODE.getKey())) {
            service.addConnector(getSecureConnector(config));
            insecureConnector.setRedirectPort(config.getInt(TomcatConfig.TOMCAT_PORT_SECURE.getKey()));
        }

        final String webappDir = config.getString(TomcatConfig.TOMCAT_WEBAPP_DIR.getKey());
        final Context context = this.tomcat.addContext("", webappDir);
        context.addWelcomeFile("/index.jsp");

        context.getServletContext().setAttribute(ConfigFactory.CONFIG_FACTORY_CONFIG, config);
        context.getServletContext().setAttribute(ActorSystemFactory.ACTOR_SYSTEM_FACTORY_CONFIG, actorSystem);

        addServletWrappers(config, context);
        context.addMimeMapping("css", "text/css");
        context.addMimeMapping("js", "application/javascript");
    }

    /**
     * Start the embedded tomcat server.
     *
     * @throws LifecycleException if there is a problem starting the Tomcat server
     */
    public void start() throws LifecycleException {
        this.tomcat.start();
    }

    /**
     * Stop the embedded tomcat server.
     *
     * @throws LifecycleException if there is a problem stopping the Tomcat server
     */
    public void stop() throws LifecycleException {
        this.tomcat.stop();
    }

    protected Connector getInsecureConnector(final Config config) {
        final Connector httpConnector = new Connector(Http11NioProtocol.class.getName());
        httpConnector.setPort(config.getInt(TomcatConfig.TOMCAT_PORT_INSECURE.getKey()));
        httpConnector.setSecure(false);
        httpConnector.setScheme("http");
        addCompressionAttributes(httpConnector);
        return httpConnector;
    }

    protected Connector getSecureConnector(final Config config) {
        final Connector httpsConnector = new Connector(Http11NioProtocol.class.getName());
        httpsConnector.setPort(config.getInt(TomcatConfig.TOMCAT_PORT_SECURE.getKey()));
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("SSLEnabled", true);
        httpsConnector.setAttribute("keyAlias", config.getString(TomcatConfig.TOMCAT_SSL_KEY_ALIAS.getKey()));
        httpsConnector.setAttribute("keystorePass", config.getString(TomcatConfig.TOMCAT_SSL_KEYSTORE_PASS.getKey()));
        httpsConnector.setAttribute("keystoreFile", config.getString(TomcatConfig.TOMCAT_SSL_KEYSTORE_FILE.getKey()));
        addCompressionAttributes(httpsConnector);
        return httpsConnector;
    }

    protected void addCompressionAttributes(final Connector connector) {
        connector.setAttribute("compression", "on");
        connector.setAttribute("compressionMinSize", "2048");
        connector.setAttribute("noCompressionUserAgents", "gozilla, traviata");
        connector.setAttribute(
                "compressableMimeType", StringUtils.join(Arrays
                        .asList("text/html", "text/plain", "text/css", "text/javascript", "application/json",
                                "application/xml"), ","));
        connector.setAttribute("useSendfile", "false");
    }

    protected void addServletWrappers(final Config config, final Context context) {
        final Wrapper defaultServlet = getDefaultServletWrapper(context);
        context.addChild(defaultServlet);
        context.addServletMapping("/", defaultServlet.getName());

        final Wrapper jspServlet = getJspServletWrapper(config, context);
        context.addChild(jspServlet);
        context.addServletMapping("*.jsp", jspServlet.getName());

        final Wrapper jerseyServlet = getJerseyServletWrapper(context);
        context.addChild(jerseyServlet);
        context.addServletMapping("/rest/*", jerseyServlet.getName());
    }

    protected Wrapper getDefaultServletWrapper(final Context context) {
        final Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("default");
        defaultServlet.setServletClass(DefaultServlet.class.getName());
        defaultServlet.addInitParameter("debug", "0");
        defaultServlet.addInitParameter("listings", "false");
        defaultServlet.addInitParameter("sendfileSize", "-1");
        defaultServlet.setLoadOnStartup(1);
        return defaultServlet;
    }

    protected Wrapper getJspServletWrapper(final Config config, final Context context) {
        final Wrapper defaultServlet = context.createWrapper();
        defaultServlet.setName("jsp");
        defaultServlet.setServletClass(JspServlet.class.getName());
        defaultServlet.addInitParameter("classdebuginfo", "false");
        defaultServlet.addInitParameter("development",
                String.valueOf(config.getBoolean(TomcatConfig.TOMCAT_DEVELOPMENT_MODE.getKey())));
        defaultServlet.addInitParameter("fork", "false");
        defaultServlet.setLoadOnStartup(3);
        return defaultServlet;
    }

    protected Wrapper getJerseyServletWrapper(final Context context) {
        final Wrapper jerseyServlet = context.createWrapper();
        jerseyServlet.setName("jersey");
        jerseyServlet.setServletClass(ServletContainer.class.getName());
        jerseyServlet.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, Application.class.getName());
        jerseyServlet.setLoadOnStartup(1);
        return jerseyServlet;
    }
}
