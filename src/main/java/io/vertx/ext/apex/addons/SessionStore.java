package io.vertx.ext.apex.addons;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.apex.addons.impl.sessionstore.LocalStoreImpl;
import io.vertx.ext.apex.addons.impl.sessionstore.SharedDataStoreImpl;

/**
 * An interface to abstract session stores
 * 
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
@VertxGen
public interface SessionStore {

  public static SessionStore clusteredStore(Vertx vertx) {
    return new SharedDataStoreImpl(vertx);
  }

  public static SessionStore store() {
    return new LocalStoreImpl();
  }

  /**
   * Whether the implementation of the {@link SessionStore} supports automatic TTL for {@link Session}, i.e.: that
   * sessions are expired (and destroyed) automatically by the store.
   * 
   * @return <code>true</code> if store supports automatic expiration, <code>false</code> otherwise
   */
  boolean supportsAutomaticExpiration();

  /**
   * Gets the {@link Session} with the given {@code sessionId}, asynchronously
   * 
   * @param sessionId
   *          The id of the {@link Session} to retrieve
   * @param resultHandler
   *          The handler to process the result
   */
  void get(String sessionId, Handler<AsyncResult<Session>> resultHandler);

  /**
   * Stores the given {@link Session}, asynchronously
   * 
   * @param session
   *          The {@link Session} to store
   * @param resultHandler
   *          The handler which will get notified asynchronously when the session has been stored
   */
  void put(Session session, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Touches the {@link Session} with the given id, if exists in the store
   * 
   * @param sessionId
   *          The id of the {@link Session} to touch
   * @param resultHandler
   *          The handler which will get notified when the session has been touched
   */
  void touch(String sessionId, Handler<AsyncResult<Void>> resultHandler);

  /**
   * Touches the {@link Session} with the given id, if exists in the store
   * 
   * @param sessionId
   *          The id of the {@link Session} to destroy
   * @param resultHandler
   *          The handler which will get notified when the session has been destroyed. This handler will get the
   *          destroyed Session if existed
   */
  void remove(String sessionId, Handler<AsyncResult<Session>> resultHandler);

}
