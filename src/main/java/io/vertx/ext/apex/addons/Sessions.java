package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.apex.addons.impl.SessionsImpl;
import io.vertx.ext.apex.core.RoutingContext;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
@VertxGen
public interface Sessions extends Handler<RoutingContext> {

  public static final String APEX_SESSION_COOKIE_NAME = "__apex_session";

  static Sessions sessions(int ttlSeconds, SessionStore store) {
    return new SessionsImpl(ttlSeconds, store);
  }

  static Sessions sessions(SessionStore store) {
    return new SessionsImpl(10, store);
  }

  static Sessions sessions(int ttlSeconds) {
    return new SessionsImpl(ttlSeconds, SessionStore.store());
  }

  void createSession(Handler<AsyncResult<Session>> resultHandler);

  void getSession(Handler<AsyncResult<Session>> resultHandler);

  void touchSession();

  void destroySession(Handler<AsyncResult<Session>> resultHandler);

  @Override
  void handle(RoutingContext event);

}
