package io.vertx.ext.apex.addons.test;

import io.vertx.core.Vertx;
import io.vertx.ext.apex.addons.SessionStore;

public class SessionTestClustered extends SessionsTestBase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    startNodes(this.getNumNodes());
  }

  protected int getNumNodes() {
    return 1;
  }

  protected Vertx getVertx() {
    return vertices[0];
  }

  @Override
  public SessionStore getStore() {
    return SessionStore.clusteredStore(this.getVertx());
  }
}
