package infrastructure.twitter

import javax.inject.Inject

import play.api.Configuration
import play.api.cache.SyncCacheApi
import twitter4j.{Twitter, TwitterFactory}
import twitter4j.auth.AccessToken

import scala.concurrent.duration._
import scala.util.control.NonFatal

class TwitterAuthenticator @Inject() (
                                       configuration: Configuration,
                                       cache: SyncCacheApi
                                     ) {

  val CacheKeyPrefixTwitter = "twitterInstance"

  val ConsumerKey = configuration.get[String]("mojipic.consumerkey")
  val ConsumerSecret = configuration.get[String]("mojipic.consumersecret")

  private[this] def cacheKeyTwitter(sessionId: String): String = CacheKeyPrefixTwitter + sessionId

  /**
    * Twitterの認証を開始する
    * @param sessionId Twitterの認証をしたいセッションID
    * @param callbackUrl コールバックURL
    * @return 投稿者に認証してもらうためのURL
    * @throws TwitterException 何らかの理由でTwitterの認証を開始できなかった
    */
  def startAuthentication(sessionId: String, callbackUrl: String): String =
    try {
      val twitter = new TwitterFactory().getInstance()
      twitter.setOAuthConsumer(
        ConsumerKey,
        ConsumerSecret
      )
      val requestToken = twitter.getOAuthRequestToken(callbackUrl)
      cache.set(cacheKeyTwitter(sessionId), twitter, 30.seconds)
      requestToken.getAuthenticationURL
    } catch {
      case NonFatal(e) =>
        throw TwitterException(s"Could not get a request token. SessionId: $sessionId", e)
    }

  /**
    * Twitterのアクセストークンを取得する
    * @param sessionId Twitterの認証をしたいセッションID
    * @param verifier OAuth Verifier
    * @return アクセストークン
    * @throws TwitterException 何らかの理由でTwitterのアクセストークンを取得できなかった
    */
  def getAccessToken(sessionId: String, verifier: String): AccessToken =
    try {
      cache.get[Twitter](cacheKeyTwitter(sessionId)).get.getOAuthAccessToken(verifier)
    } catch {
      case NonFatal(e) =>
        throw TwitterException(s"Could not get an access token. SessionId: $sessionId", e)
    }
}

case class TwitterException(message: String = null, cause: Throwable = null)
  extends RuntimeException(message, cause)
