package cc.jbx.renetty.server;

import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.eclipse.jetty.http2.FlowControlStrategy;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class AppServer {

    public static void main(String []args) throws Exception {
        final InputStream is = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("logger.properties");
        final LogManager manager = LogManager.getLogManager();
        manager.readConfiguration(is);

        final Logger logger = Logger.getLogger(AppServer.class.getCanonicalName());

        logger.info("starting server...");

        HTTP2ServerConnectionFactory connectionFactory = new HTTP2CServerConnectionFactory(new HttpConfiguration());
        connectionFactory.setInitialSessionRecvWindow(FlowControlStrategy.DEFAULT_WINDOW_SIZE);
        connectionFactory.setInitialStreamRecvWindow(FlowControlStrategy.DEFAULT_WINDOW_SIZE);
        QueuedThreadPool serverExecutor = new QueuedThreadPool();

        serverExecutor.setName("server");
        Server server = new Server(serverExecutor);
        ServerConnector connector = new ServerConnector(server, 1, 1, connectionFactory);
        connector.setPort(8080);
        server.addConnector(connector);
        Servlet servlet = new HeloServlet();
        ServletContextHandler context = new ServletContextHandler(server, "/", true, false);
        context.addServlet(new ServletHolder(servlet), "/*");

        server.start();
    }
}