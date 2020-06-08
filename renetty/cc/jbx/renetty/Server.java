package cc.jbx.renetty;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;

public class Server {

    public static void main(String []args) throws IOException {
        final InputStream is = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("logger.properties");
        final LogManager manager = LogManager.getLogManager();
        manager.readConfiguration(is);
        final Logger logger = Logger.getLogger(Server.class.getCanonicalName());

        logger.info("starting server...");
        final DisposableServer server =
            HttpServer.create()
                .port(8080)
                .protocol(HttpProtocol.H2C)
                .handle((request, response) -> response.sendString(Mono.just("hello")))
                .bindNow();

        logger.info("listening on 127.0.0.1:8080");
        server.onDispose()
            .block();
    }
}