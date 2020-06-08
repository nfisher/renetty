package cc.jbx.renetty;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
import org.eclipse.jetty.http2.api.server.ServerSessionListener;
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
        new ServerSessionListener.Adapter(),
        sessionPromise);

    Session session = sessionPromise.get(5, TimeUnit.SECONDS);

    HttpFields requestFields = new HttpFields();
    requestFields.put("User-Agent", client.getClass().getName() + "/" + Jetty.VERSION);

    HttpURI uri = new HttpURI("http://127.0.0.1:8080/");
    MetaData.Request request = new MetaData.Request("GET", uri, HttpVersion.HTTP_2, requestFields);
    HeadersFrame headersFrame = new HeadersFrame(request, null, false);
    CountDownLatch latch = new CountDownLatch(1);
    AtomicReference<String> ref = new AtomicReference<>();

    Stream.Listener responseListener =
        new Stream.Listener.Adapter() {
          @Override
          public void onData(Stream stream, DataFrame frame, Callback callback) {
            logger.warning(frame.toString());
            callback.succeeded();
            ByteBuffer buf = frame.getData();
            ref.set(UTF_8.decode(buf).toString());
            latch.countDown();
          }

          @Override
          public void onHeaders(Stream stream, HeadersFrame frame) {
            logger.warning(frame.toString());
          }
        };


    FuturePromise<Stream> streamPromise = new FuturePromise<>();
    session.newStream(headersFrame, streamPromise, responseListener);
    Stream stream = streamPromise.get(5, TimeUnit.SECONDS);
    ByteBuffer content = ByteBuffer.allocate(8);
    DataFrame requestContent = new DataFrame(stream.getId(), content, true);
    stream.data(requestContent, Callback.NOOP);
    latch.await();
    client.stop();

    logger.info(ref.get());
  }
}
