/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.apex;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.apex.sstore.SessionStore;
import io.vertx.ext.auth.AuthService;

import java.util.Map;

/**
 * Represents a browser session.
 * <p>
 * Sessions persist between HTTP requests for a single browser session. They are deleted when the browser is closed, or
 * they time-out. Session cookies are used to maintain sessions using a secure UUID.
 * <p>
 * Sessions can be used to maintain data for a browser session, e.g. a shopping basket.
 * <p>
 * The context must have first been routed to a {@link io.vertx.ext.apex.handler.SessionHandler}
 * for sessions to be available.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@VertxGen
public interface Session {

  /**
   * @return The unique ID of the session. This is generated using a random secure UUID.
   */
  String id();

  /**
   * Put some data in a session
   *
   * @param key  the key for the data
   * @param obj  the data
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Session put(String key, Object obj);

  /**
   * Get some data from the session
   *
   * @param key  the key of the data
   * @return  the data
   */
  <T> T get(String key);

  /**
   * Remove some data from the session
   *
   * @param key  the key of the data
   * @return  the data that was there or null if none there
   */
  <T> T remove(String key);

  /**
   * @return the session data as a map
   */
  @GenIgnore
  Map<String, Object> data();

  /**
   * @return the time the session was last accessed
   */
  long lastAccessed();

  /**
   * Destroy the session
   */
  void destroy();

  /**
   * @return has the session been destroyed?
   */
  boolean isDestroyed();

  /**
   * @return  the login ID of the logged in user (if any). Must be used in conjunction with a
   * {@link io.vertx.ext.apex.handler.AuthHandler}.
   */
  String getLoginID();

  /**
   * @return  true if the user is logged in.
   */
  boolean isLoggedIn();

  /**
   * Does the logged in user have the specified role?  Information is cached for the lifetime of the session
   *
   * @param role  the role
   * @param resultHandler will be called with a result true/false
   */
  void hasRole(String role, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Does the logged in user have the specified permissions?  Information is cached for the lifetime of the session
   *
   * @param permission  the permission
   * @param resultHandler will be called with a result true/false
   */
  void hasPermission(String permission, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Logout the user.
   */
  void logout();

  /**
   * @return the amount of time in ms, after which the session will expire, if not accessed.
   */
  long timeout();

  /**
   * @return the store for the session
   */
  SessionStore sessionStore();

  /**
   * Set the login ID for the session
   *
   * @param loginID  the login ID
   */
  void setLoginID(String loginID);

  /**
   * Mark the session as being accessed.
   */
  void setAccessed();

  /**
   * Set the auth service
   *
   * @param authService  the auth service
   */
  void setAuthService(AuthService authService);

}
