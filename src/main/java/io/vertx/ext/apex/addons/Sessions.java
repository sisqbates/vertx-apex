package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.ext.apex.addons.impl.SessionsImpl;
import io.vertx.ext.apex.core.RoutingContext;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
@VertxGen
public interface Sessions extends Handler<RoutingContext> {

  public static final String APEX_SESSION_COOKIE_NAME = "__apex_session";

  static Sessions sessions(int ttl) {
    return new SessionsImpl(ttl);
  }

  Session addSession();

  Session getSession();

  void touchSession();

  void destroySession();

  @Override
  void handle(RoutingContext event);

}
