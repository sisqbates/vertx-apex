package io.vertx.ext.apex.addons.impl.sessionstore;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.apex.addons.Session;
import io.vertx.ext.apex.addons.SessionStore;

import java.util.concurrent.CountDownLatch;

public class SharedDataStoreImpl implements SessionStore {

  private AsyncMap<String, Session> sessionsMap;

  public SharedDataStoreImpl(Vertx vertx) {
    CountDownLatch latch = new CountDownLatch(1);

    vertx.sharedData().<String, Session> getClusterWideMap("apex_sessions", map -> {
      if (map.succeeded())
        sessionsMap = map.result();
      latch.countDown();
    });
    try {
      latch.await();
      if (sessionsMap == null) {
        // TODO: How to get the error generated inside the handler?
        throw new IllegalStateException("Could not initialize SharedDataStoreImpl");
      }
    } catch (InterruptedException ie) {
      throw new IllegalStateException("Could not initialize SharedDataStoreImpl", ie);
    }
  }

  @Override
  public boolean supportsAutomaticExpiration() {
    return false;
  }

  @Override
  public void get(String sessionId, Handler<AsyncResult<Session>> resultHandler) {
    sessionsMap.get(sessionId, resultHandler);
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> resultHandler) {
    sessionsMap.put(session.getId(), session, resultHandler);
  }

  @Override
  public void touch(String sessionId, Handler<AsyncResult<Void>> resultHandler) {
    throw new IllegalArgumentException("This store does not implement expiration");
  }

  @Override
  public void remove(String sessionId, Handler<AsyncResult<Session>> resultHandler) {
    sessionsMap.remove(sessionId, resultHandler);
  }

}
