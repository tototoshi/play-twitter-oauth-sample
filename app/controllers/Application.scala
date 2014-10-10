package controllers

import play.api._
import play.api.mvc._
import play.api.libs.oauth._

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
      case Some(_) => Ok(views.html.index("Your new application is ready."))
      case None => oauth.retrieveRequestToken(oauthCallbackURL) match {
        case Right(token) =>
          Redirect(oauth.redirectUrl(token.token))
        case Left(e) => {
          InternalServerError(e.getMessage)
        }
      }
    }
  }

}
