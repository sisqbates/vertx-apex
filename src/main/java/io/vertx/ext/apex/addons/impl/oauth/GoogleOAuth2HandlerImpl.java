package io.vertx.ext.apex.addons.impl.oauth;

import io.netty.handler.codec.http.QueryStringEncoder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.core.RoutingContext;

import java.net.URLEncoder;
import java.util.Objects;
import java.util.UUID;

public class GoogleOAuth2HandlerImpl {

  private Handler<AsyncResult<JsonObject>> handler;
  private JsonObject config;

  public GoogleOAuth2HandlerImpl(Handler<AsyncResult<JsonObject>> handler, JsonObject config) {
    this.handler = handler;
    this.config = config;

    // Should we use the discovery document at https://accounts.google.com/.well-known/openid-configuration ??
    // See https://developers.google.com/accounts/docs/OpenIDConnect#discovery

  }

  private String getAuthenticateURL(String state) {

    QueryStringEncoder url = new QueryStringEncoder("https://accounts.google.com/o/oauth2/auth");
    url.addParam("client_id", config.getString("client_id"));
    url.addParam("response_type", "code");
    url.addParam("scope", "openid email");
    url.addParam("redirect_uri", this.getRedirectURL());
    url.addParam("state", state);

    return url.toString();
  }

  private String getRedirectURL() {
    return "http://127.0.0.1:8888/auth/google/callback";
  }

  public void handleInitialRedirect(RoutingContext context) {
    context.response().setStatusCode(302)
        .putHeader("location", this.getAuthenticateURL(this.generateStateToken(context))).end();
  }

  private String generateStateToken(RoutingContext context) {
    String state = UUID.randomUUID().toString();
    context.session().data().put("OAuth2 state", state);
    return state;
  }

  private String getStateToken(RoutingContext context) {
    return context.session().data().getString("OAuth2 state");
  }

  public void handleCallback(RoutingContext context) {

    String state = context.request().params().get("state");
    if (!Objects.equals(state, this.getStateToken(context))) {
      handler.handle(Future.failedFuture("Invalid state token returned from authentication service"));
    }

    String code = context.request().params().get("code");
    if (code == null) {
      handler.handle(Future.failedFuture("No one-time authorization code received"));
    }

    try {
      StringBuilder params = new StringBuilder();
      params.append("code=").append(URLEncoder.encode(code, "UTF-8"));
      params.append("&client_id=").append(URLEncoder.encode(config.getString("client_id"), "UTF-8"));
      params.append("&client_secret=").append(URLEncoder.encode(config.getString("client_secret"), "UTF-8"));
      params.append("&redirect_uri=").append(URLEncoder.encode(this.getRedirectURL(), "UTF-8"));
      params.append("&grant_type=authorization_code");

      HttpClient client = context.vertx().createHttpClient(new HttpClientOptions().setSsl(true));
      client.request(HttpMethod.POST, 443, "www.googleapis.com", "/oauth2/v3/token", this.dataResponse(context))
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded").end(params.toString(), "UTF-8");

    } catch (Exception e) {
      handler.handle(Future.failedFuture("Cannot encode params to get data from user"));
    }

  }

  private Handler<HttpClientResponse> dataResponse(RoutingContext context) {
    return (HttpClientResponse res) -> {
      if (res.statusCode() != 200) {
        context.response().end("Error: " + res.toString());
      } else {
        res.bodyHandler(buf -> {
          JsonObject j = new JsonObject(buf.toString("UTF-8"));
          context.response().end("Success: " + j.getString("id_token"));
        });
      }
    };
  }

}
