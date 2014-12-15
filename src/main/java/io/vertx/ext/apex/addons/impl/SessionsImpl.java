package io.vertx.ext.apex.addons.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.apex.addons.Cookie;
import io.vertx.ext.apex.addons.Cookies;
import io.vertx.ext.apex.addons.Session;
import io.vertx.ext.apex.addons.SessionStore;
import io.vertx.ext.apex.addons.Sessions;
import io.vertx.ext.apex.core.RoutingContext;

import java.util.Objects;
import java.util.UUID;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
public class SessionsImpl implements Sessions {

  private static final Logger log = LoggerFactory.getLogger(SessionsImpl.class);

  private int ttlSeconds = 60 * 30;
  private SessionStore store;

  public SessionsImpl(int ttlSeconds, SessionStore store) {
    if (ttlSeconds < 1)
      throw new IllegalArgumentException("ttlSeconds must be >= 0");
    this.ttlSeconds = ttlSeconds;
    this.store = Objects.requireNonNull(store, "store cannot be null");
  }

  @Override
  public void createSession(Handler<AsyncResult<Session>> resultHandler) {
    this.destroySession(null);

    Session session = Session.session(UUID.randomUUID().toString(), System.currentTimeMillis(), this.ttlSeconds);
    store.put(session, result -> {
      if (result.succeeded()) {
        Cookies.addCookie(Cookie.cookie(APEX_SESSION_COOKIE_NAME, session.getId()));
        resultHandler.handle(Future.succeededFuture(session));
      } else {
        resultHandler.handle(Future.failedFuture(result.cause()));
      }
    });
  }

  @Override
  public void getSession(Handler<AsyncResult<Session>> resultHandler) {
    Cookie sessionCookie = Cookies.getCookie(APEX_SESSION_COOKIE_NAME);
    if (sessionCookie == null) {
      resultHandler.handle(Future.succeededFuture(null));
      return;
    }

    store.get(sessionCookie.getValue(), session -> {
      forwardResult(session, resultHandler);
    });
  }

  @Override
  public void touchSession() {
    Cookie sessionCookie = Cookies.getCookie(APEX_SESSION_COOKIE_NAME);
    if (sessionCookie == null)
      return;

    if (store.supportsAutomaticExpiration()) {
      store.touch(sessionCookie.getValue(), result -> {
        if (result.failed()) {
          log.warn("Could not touch session", result.cause());
        }
      });
    } else {
      store.get(sessionCookie.getValue(), session -> {
        if (session.succeeded()) {
          if (session.result() != null) {
            if (session.result().isExpired()) {
              this.destroySession(null);
            } else {
              session.result().touch();
            }
          }
        } else {
          log.warn("Could not touch session", session.cause());
        }
      });
    }
  }

  @Override
  public void destroySession(Handler<AsyncResult<Session>> resultHandler) {
    Cookie sessionCookie = Cookies.getCookie(APEX_SESSION_COOKIE_NAME);
    if (sessionCookie != null) {
      store.remove(sessionCookie.getValue(), session -> {
        if (session.succeeded()) {
          Cookies.removeCookie(APEX_SESSION_COOKIE_NAME);
        }
        forwardResult(session, resultHandler);
      });
    }
  }

  private <T> void forwardResult(AsyncResult<T> rc, Handler<AsyncResult<T>> resultHandler) {
    if (resultHandler == null)
      return;

    if (rc.succeeded()) {
      resultHandler.handle(Future.succeededFuture(rc.result()));
    } else {
      resultHandler.handle(Future.failedFuture(rc.cause()));
    }
  }

  @Override
  public void handle(RoutingContext context) {
    touchSession();
    context.next();
  }

}
