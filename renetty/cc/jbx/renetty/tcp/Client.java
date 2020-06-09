package cc.jbx.renetty.tcp;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.reactivestreams.Publisher;

import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.netty.tcp.TcpClient;


class Sender {
  private final Flux<Long> send = Flux.interval(Duration.ofSeconds(1L));
  private final String prefix;

  public Sender(final String prefix) {
    this.prefix = prefix;
  }

  public Flux<String> getSend() {
    return send.map(l -> prefix + l);
  }
}

public class Client {

  private static final Logger LOGGER = Logger.getLogger(Client.class.getCanonicalName());
  private static final Sender sender = new Sender("foobar-");

  public static void logResponse(ByteBuf byteBuf) {
    String s = UTF_8.decode(byteBuf.nioBuffer()).toString();
    LOGGER.info(s);
  }

  static Publisher<Void> handler(NettyInbound in, NettyOutbound out) {
    in.receive().subscribe(Client::logResponse);
    return out.sendString(sender.getSend());
  }

  public static void main(String[] args) throws IOException {
    final InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("logger.properties");
    final LogManager manager = LogManager.getLogManager();
    manager.readConfiguration(is);

    LOGGER.info("starting client...");

    Connection connection =
        TcpClient.create()
            .host("localhost")
            .port(8080)
            .wiretap(true)
            .handle(Client::handler)
            .connectNow();

    LOGGER.info("connecting to localhost:8080");

    connection
        .onDispose()
        .block();
  }
}
