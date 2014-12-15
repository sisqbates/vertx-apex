package io.vertx.ext.apex.addons.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.Cookies;
import io.vertx.ext.apex.addons.SessionStore;
import io.vertx.ext.apex.addons.Sessions;
import io.vertx.ext.apex.test.ApexTestBase;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
public class SessionsTestBase extends ApexTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    router.route().handler(Cookies.cookies());
  }

  public SessionStore getStore() {
    return SessionStore.store();
  }

  @Test
  public void testNoSession() throws Exception {
    Sessions sessions = Sessions.sessions(10, this.getStore());
    router.route().handler(sessions);

    router.route().handler(rc -> {
      sessions.getSession(s -> {
        assertTrue(s.succeeded());
        assertNull(s.result());
        rc.response().end();
      });
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");
    testRequestWithCookies(HttpMethod.GET, "/", Sessions.APEX_SESSION_COOKIE_NAME + "=not-a-valid-session-id", 200,
        "OK");
  }

  @Test
  public void testSessionCreation() throws Exception {
    Sessions sessions = Sessions.sessions(10, this.getStore());
    router.route().handler(sessions);

    router.route().handler(rc -> {
      sessions.createSession(session -> {
        assertTrue(session.succeeded());
        assertNotNull(session.result());
        rc.response().end();
      });
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");

  }

  @Test
  public void testNotExpiredSession() throws Exception {
    Sessions sessions = Sessions.sessions(10, this.getStore());
    router.route().handler(sessions);

    final Set<String> sessionIds = new HashSet<>();
    CountDownLatch latch = new CountDownLatch(1);
    router.route().handler(rc -> {
      if (sessionIds.isEmpty()) {
        sessions.createSession((session) -> {
          assertTrue(session.succeeded());
          sessionIds.add(session.result().getId());
          latch.countDown();
        });
      } else {
        sessions.getSession((session) -> {
          assertTrue(session.succeeded());
          assertEquals(sessionIds.iterator().next(), session.result().getId());
        });
      }
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    latch.await();
    testRequestWithCookies(HttpMethod.GET, "/", Sessions.APEX_SESSION_COOKIE_NAME + "=" + sessionIds.iterator().next(),
        200, "OK");

  }

  @Test
  public void testExpiredSession() throws Exception {
    Sessions sessions = Sessions.sessions(1, this.getStore());
    router.route().handler(sessions);

    final Set<String> sessionIds = new HashSet<>();
    CountDownLatch latch = new CountDownLatch(1);
    router.route().handler(rc -> {
      if (sessionIds.isEmpty()) {
        sessions.createSession((session) -> {
          assertTrue(session.succeeded());
          sessionIds.add(session.result().getId());
          latch.countDown();
        });

      } else {
        sessions.getSession((session) -> {
          assertTrue(session.succeeded());
          assertNull(session.result());
        });
      }
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    latch.await();
    Thread.sleep(5000);
    testRequestWithCookies(HttpMethod.GET, "/", Sessions.APEX_SESSION_COOKIE_NAME + "=" + sessionIds.iterator().next(),
        200, "OK");

  }

}
