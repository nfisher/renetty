package cc.jbx.renetty.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.http2.api.Session;
import org.eclipse.jetty.http2.api.Stream;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.frames.DataFrame;
import org.eclipse.jetty.http2.frames.HeadersFrame;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.FuturePromise;
import org.eclipse.jetty.util.Jetty;

public class Client {
  public static void main(String[] args) throws Exception {
    final InputStream is =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("logger.properties");
    final LogManager manager = LogManager.getLogManager();
    manager.readConfiguration(is);
    final Logger logger = Logger.getLogger(Client.class.getCanonicalName());

    logger.info("starting client...");

    FuturePromise<Session> sessionPromise = new FuturePromise<>();
    HTTP2Client client = new HTTP2Client();
    client.start();
    client.connect(
        new InetSocketAddress("127.0.0.1", 8080),
        new Session.Listener.Adapter(),
        sessionPromise);

    Session session = sessionPromise.get(5, TimeUnit.SECONDS);

    HttpFields requestFields = new HttpFields();
    requestFields.put("User-Agent", client.getClass().getName() + "/" + Jetty.VERSION);

    HttpURI uri = new HttpURI("http://127.0.0.1:8080/");
    MetaData.Request request = new MetaData.Request("GET", uri, HttpVersion.HTTP_2, requestFields);
    HeadersFrame headersFrame = new HeadersFrame(request, null, false);
    int calls = 10;
    CountDownLatch latch = new CountDownLatch(calls);
    AtomicReference<String> ref = new AtomicReference<>();

    Stream.Listener responseListener =
        new Stream.Listener.Adapter() {
          @Override
          public void onData(Stream stream, DataFrame frame, Callback callback) {
            logger.warning(frame.toString());

            callback.succeeded();
            ByteBuffer buf = frame.getData();
            String s = UTF_8.decode(buf).toString();
            logger.info(s);
            ref.set(s);

            if (frame.isEndStream()) {
              logger.warning("endStream");
              latch.countDown();
            }
          }

          @Override
          public void onHeaders(Stream stream, HeadersFrame frame) {
            logger.warning(frame.toString());
          }
        };

    for (int i = 0; i < calls; i++) {
      FuturePromise<Stream> streamPromise = new FuturePromise<>();
      session.newStream(headersFrame, streamPromise, responseListener);

      Stream stream = streamPromise.get(5, TimeUnit.SECONDS);
      ByteBuffer content = UTF_8.encode("helo");
      DataFrame requestContent = new DataFrame(stream.getId(), content, true);
      stream.data(requestContent, Callback.NOOP);
    }
    latch.await();
    logger.info("<" + ref.get() + ">");
    client.stop();
  }
}
