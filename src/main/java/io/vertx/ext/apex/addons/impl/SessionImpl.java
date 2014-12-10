package io.vertx.ext.apex.addons.impl;

import io.vertx.ext.apex.addons.Session;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
public class SessionImpl implements Session {

  private String id;
  private long lastTouched;
  private int ttlSeconds;

  public SessionImpl(String id, long lastTouched, int ttlSeconds) {
    this.id = id;
    this.lastTouched = lastTouched;
    this.ttlSeconds = ttlSeconds;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public boolean isExpired() {
    return lastTouched + ttlSeconds * 1000 < System.currentTimeMillis();
  }

  @Override
  public void touch() {
    this.lastTouched = System.currentTimeMillis();
  }

}
