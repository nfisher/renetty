package cc.jbx.renetty.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class HeloServlet extends HttpServlet {
  final Logger logger = Logger.getLogger(HeloServlet.class.getCanonicalName());
  private AtomicInteger counter = new AtomicInteger();

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    int c = counter.getAndIncrement();
    logger.warning("received request " + c);
    response.getWriter().write("hello world " + c);
  }
}
