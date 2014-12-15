package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.apex.addons.impl.SessionImpl;

import java.io.Serializable;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
@VertxGen
public interface Session extends Serializable {

  static Session session(String id, long lastTouched, int ttl) {
    return new SessionImpl(id, lastTouched, ttl);
  }

  String getId();

  boolean isExpired();

  void touch();
}
