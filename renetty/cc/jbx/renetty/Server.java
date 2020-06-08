package cc.jbx.renetty;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;

public class Server {
    public static void main(String []args) {
        DisposableServer server =
            HttpServer.create()
                .port(8080)
                .protocol(HttpProtocol.H2C)
                .handle((request, response) -> response.sendString(Mono.just("hello")))
                .bindNow();

        System.out.println("Listening on 127.0.0.1:8080");
        server.onDispose()
            .block();
    }
}