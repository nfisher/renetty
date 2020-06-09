package cc.jbx.renetty.tcp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpServer;

public class App {
  private static final Logger LOGGER = Logger.getLogger(App.class.getCanonicalName());

  public static void main(String [] args) throws IOException {
    final InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("logger.properties");
    final LogManager manager = LogManager.getLogManager();
    manager.readConfiguration(is);


    LOGGER.info("starting server...");

    final DisposableServer server =
        TcpServer.create()
            .host("localhost")
            .port(8080)
            .wiretap(true)
            .handle(App::handler)
            .bindNow();

    LOGGER.info("binding localhost:8080");

    server
        .onDispose()
        .block();
  }

  static Publisher<Void> handler(NettyInbound in, NettyOutbound out) {
    in.receive().subscribe(byteBuf -> LOGGER.info(UTF_8.decode(byteBuf.nioBuffer()).toString()));
    return out.sendString(Flux.interval(Duration.ofMillis(250)).map(l -> "" + l));
  }
}
