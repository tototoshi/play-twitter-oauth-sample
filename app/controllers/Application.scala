package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.oauth._
import _root_.libs.{ Jade, Twitter, TwitterUser }

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

  private def getAuthUser(request: RequestHeader): Option[TwitterUser] =
    for {
      id <- request.session.get("id")
      secret <- request.session.get("secret")
    } yield {
      val twitter = new Twitter(id, secret)
      twitter.user
    }

  def index = Action { implicit request =>
    val user = getAuthUser(request)
    Ok(
      Jade.render(
        "index.jade",
        Map(
          "isLoggedIn" -> user.isDefined,
          "screenName" -> user.map(_.screenName)
        )
      )
    )
  }

  def login = Action { implicit request =>
    getAuthUser(request) match {
      case Some(_) => Redirect(routes.Application.index)
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

  def authorize = Action { implicit request =>
    val form = Form(
      tuple(
        "oauth_token" -> optional(nonEmptyText),
        "oauth_verifier" -> optional(nonEmptyText),
        "denied" -> optional(nonEmptyText)
      )
    )

    form.bindFromRequest.fold({
      formWithError => BadRequest
    }, {
      case (Some(oauthToken), Some(oauthVerifier), None) =>
        (for {
          tokenSecret <- request.session.get("twitter.requestTokenSecret")
          requestToken = RequestToken(oauthToken, tokenSecret)
          token <- oauth.retrieveAccessToken(
            requestToken, oauthVerifier
          ).right.toOption
        } yield {
          Redirect(routes.Application.index).withSession(
            "id" -> token.token,
            "secret" -> token.secret
          )
        }).getOrElse(BadRequest)
      case (None, None, Some(denied)) => Redirect(routes.Application.index)
      case _ => BadRequest
    })
  }

  def denied(denied: String) = Action { implicit request =>
    Redirect(routes.Application.index)
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.index).withNewSession
  }

}
