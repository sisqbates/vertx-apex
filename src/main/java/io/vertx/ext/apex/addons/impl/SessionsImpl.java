package io.vertx.ext.apex.addons.impl;

import io.vertx.ext.apex.addons.Cookie;
import io.vertx.ext.apex.addons.Cookies;
import io.vertx.ext.apex.addons.Session;
import io.vertx.ext.apex.addons.Sessions;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
public class SessionsImpl implements Sessions {

  private int ttlSeconds = 60 * 30;

  private ConcurrentHashMap<String, Session> store = new ConcurrentHashMap<>();

  public SessionsImpl(int ttlSeconds) {
    if (ttlSeconds < 1)
      throw new IllegalArgumentException("ttlSeconds must be >= 0");
    this.ttlSeconds = ttlSeconds;
  }

  @Override
  public Session addSession() {
    this.destroySession();

    String sessionId = UUID.randomUUID().toString();
    Session result = Session.session(sessionId, System.currentTimeMillis(), this.ttlSeconds);
    store.put(sessionId, result);
    Cookies.addCookie(Cookie.cookie(APEX_SESSION_COOKIE_NAME, sessionId));
    return result;
  }

  @Override
  public Session getSession() {
    Cookie sessionCookie = Cookies.getCookie(APEX_SESSION_COOKIE_NAME);
    if (sessionCookie == null)
      return null;

    Session result = store.get(sessionCookie.getValue());
    if (result != null && result.isExpired()) {
      this.destroySession();
      result = null;
    }
    return result;
  }

  @Override
  public void touchSession() {
    Session session = this.getSession();
    if (session != null)
      session.touch();
  }

  @Override
  public void destroySession() {
    Cookie sessionCookie = Cookies.getCookie(APEX_SESSION_COOKIE_NAME);
    if (sessionCookie != null) {
      store.remove(sessionCookie.getValue());
      Cookies.removeCookie(APEX_SESSION_COOKIE_NAME);
    }
  }

  @Override
  public void handle(RoutingContext context) {
    touchSession();
    context.next();
  }

}
