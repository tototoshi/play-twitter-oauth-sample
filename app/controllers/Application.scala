package controllers

import play.api._
import play.api.mvc._
import play.api.libs.oauth._
import _root_.libs.Jade

object Application extends Controller {

  private val twitterServiceInfo = {
    val twitterRequestTokenURL = "https://api.twitter.com/oauth/request_token"
    val twitterAccessTokenURL = "https://api.twitter.com/oauth/access_token"
    val twitterAuthorizationURL = "https://api.twitter.com/oauth/authorize"
    val twitterConsumerKey =
      ConsumerKey(
        Play.current.configuration.getString("twitter.consumerKey").get,
        Play.current.configuration.getString("twitter.consumerSecret").get
      )
    ServiceInfo(
      twitterRequestTokenURL,
      twitterAccessTokenURL,
      twitterAuthorizationURL,
      twitterConsumerKey
    )
  }

  private val oauthCallbackURL = Play.current.configuration.getString(
    "twitter.callbackURL"
  ).get

  private val oauth = OAuth(twitterServiceInfo, use10a = true)

  def index = Action { implicit request =>
    request.session.get("id") match {
      case Some(_) => Ok(Jade.render("index.jade"))
      case None => oauth.retrieveRequestToken(oauthCallbackURL) match {
        case Right(token) =>
          Redirect(oauth.redirectUrl(token.token)).withSession(
            "twitter.requestTokenSecret" -> token.secret
          )
        case Left(e) => {
          InternalServerError(e.getMessage)
        }
      }
    }
  }

  def authorize(oauthToken: String, oauthVerifier: String) =
    Action { implicit request =>
      request.session.get("twitter.requestTokenSecret") match {
        case Some(tokenSecret) =>
          val requestToken = RequestToken(oauthToken, tokenSecret)
          oauth.retrieveAccessToken(requestToken, oauthVerifier) match {
            case Right(token) =>
              Logger.info(token.toString)
              Redirect(routes.Application.index).withSession(
                "id" -> token.token
              )
            case Left(e) =>
              Logger.error(e.getMessage)
              BadRequest
          }
        case None => BadRequest
      }
    }

  def logout = Action { implicit request =>
    Ok(Jade.render("logout.jade")).withNewSession
  }

}
