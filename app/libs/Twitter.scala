package libs

import play.api.Play.current

case class TwitterUser(id: Long, screenName: String)

class Twitter(accessToken: String, accessTokenSecret: String) {

  val consumerKey = current.configuration.getString("twitter.consumerKey").get
  val consumerSecret = current.configuration.getString("twitter.consumerSecret").get

  import twitter4j._
  import twitter4j.conf._
  private val twitter4jConfBuilder = new ConfigurationBuilder
  private val twitter4jConf = twitter4jConfBuilder
    .setOAuthConsumerKey(consumerKey)
    .setOAuthConsumerSecret(consumerSecret)
    .setOAuthAccessToken(accessToken)
    .setOAuthAccessTokenSecret(accessTokenSecret)
    .build
  private val twitter = new TwitterFactory(twitter4jConf).getInstance()

  def user: TwitterUser = TwitterUser(twitter.getId, twitter.getScreenName)

}
