package io.vertx.ext.apex.addons.test;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.apex.addons.Cookies;
import io.vertx.ext.apex.addons.Sessions;
import io.vertx.ext.apex.test.ApexTestBase;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * @author <a href="mailto:sisqbates@gmail.com">Ferran Puig</a>
 */
public class SessionsTest extends ApexTestBase {

  private Sessions sessions;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    sessions = Sessions.sessions(10);
    router.route().handler(Cookies.cookies());
    router.route().handler(sessions);
  }

  @Test
  public void testNoSession() throws Exception {

    router.route().handler(rc -> {
      assertNull(sessions.getSession());
      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");
    testRequestWithCookies(HttpMethod.GET, "/", Sessions.APEX_SESSION_COOKIE_NAME + "=not-a-valid-session-id", 200,
        "OK");
  }

  @Test
  public void testSessionCreation() throws Exception {
    Sessions sessions = Sessions.sessions(10);
    router.route().handler(sessions);

    router.route().handler(rc -> {
      sessions.addSession();
      assertNotNull(sessions.getSession());

      rc.response().end();
    });

    testRequest(HttpMethod.GET, "/", 200, "OK");
  }

  @Test
  public void testSessionKeepAlive() throws Exception {
    Sessions sessions = Sessions.sessions(10);
    router.route().handler(sessions);

    final Set<String> sessionIds = new HashSet<>();
    router.route().handler(rc -> {
      if (sessionIds.isEmpty()) {
        sessions.addSession();
        sessionIds.add(sessions.getSession().getId());
      } else {
        assertNotNull(sessions.getSession());
        assertEquals(sessionIds.iterator().next(), sessions.getSession().getId());
      }
      rc.response().end();
    });
    testRequest(HttpMethod.GET, "/", 200, "OK");
    testRequestWithCookies(HttpMethod.GET, "/", Sessions.APEX_SESSION_COOKIE_NAME + "=" + sessionIds.iterator().next(),
        200, "OK");

  }
}
