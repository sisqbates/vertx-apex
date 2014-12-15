package io.vertx.ext.apex.addons.impl.sessionstore;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.apex.addons.Session;
import io.vertx.ext.apex.addons.SessionStore;

import java.util.concurrent.ConcurrentHashMap;

public class LocalStoreImpl implements SessionStore {

  private ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

  @Override
  public boolean supportsAutomaticExpiration() {
    return false;
  }

  @Override
  public void get(String sessionId, Handler<AsyncResult<Session>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(sessions.get(sessionId)));
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {
    sessions.put(session.getId(), session);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void touch(String sessionId, Handler<AsyncResult<Void>> resultHandler) {
    if (sessions.containsKey(sessionId)) {
      sessions.get(sessionId).touch();
    }
    resultHandler.handle(Future.succeededFuture());
  }

  @Override
  public void remove(String sessionId, Handler<AsyncResult<Session>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(sessions.remove(sessionId)));
  }

}
