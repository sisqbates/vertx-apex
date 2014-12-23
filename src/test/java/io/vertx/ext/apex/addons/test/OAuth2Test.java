package io.vertx.ext.apex.addons.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.addons.LocalSessionStore;
import io.vertx.ext.apex.addons.SessionHandler;
import io.vertx.ext.apex.addons.impl.oauth.GoogleOAuth2HandlerImpl;
import io.vertx.ext.apex.core.CookieHandler;
import io.vertx.ext.apex.core.Router;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.junit.Test;

public class OAuth2Test {

  @Test
  public void testOAuth() throws Exception {

    Vertx v = Vertx.vertx();
    HttpServer server = v.createHttpServer(new HttpServerOptions().setPort(8888));

    JsonObject config = new JsonObject();
    config.put("client_id", System.getProperty("client_id"));
    config.put("client_secret", System.getProperty("client_secret"));

    GoogleOAuth2HandlerImpl oauth = new GoogleOAuth2HandlerImpl(res -> {
      System.out.println(res);
    }, config);

    Router router = Router.router(v);
    router.route().handler(CookieHandler.cookieHandler());
    router.route().handler(SessionHandler.sessionHandler(LocalSessionStore.localSessionStore(v)));

    Router subrouter = Router.router(v);
    subrouter.route("/callback").handler(oauth::handleCallback);
    subrouter.route().handler(oauth::handleInitialRedirect);

    router.mountSubRouter("/auth/google", subrouter);

    CountDownLatch latch = new CountDownLatch(2);
    server.requestHandler(router::accept).listen(onSuccess(res -> {
      latch.countDown();
    }));
    latch.await();
  }

  protected <T> Handler<AsyncResult<T>> onSuccess(Consumer<T> consumer) {
    return result -> {
      if (result.failed()) {
        result.cause().printStackTrace();
      } else {
        consumer.accept(result.result());
      }
    };
  }

}
