package controllers

import java.util.UUID

import play.api.cache.SyncCacheApi
import play.api.mvc._
import twitter4j.auth.AccessToken

import scala.concurrent.{ExecutionContext, Future}

case class TwitterLoginRequest[A](sessionId: String, accessToken: Option[AccessToken], request: Request[A]) extends WrappedRequest[A](request)

abstract class TwitterLoginController(protected val cc: ControllerComponents) extends AbstractController(cc) {
  val cache: SyncCacheApi
  val sessionIdName = "mojipic.sessionId"

  def TwitterLoginAction = new ActionBuilder[TwitterLoginRequest, AnyContent] {
    override protected def executionContext: ExecutionContext = cc.executionContext

    override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

    def invokeBlock[A](request: Request[A], block: TwitterLoginRequest[A] => Future[Result]) = {
      val sessionIdOpt = request.cookies.get(sessionIdName).map(_.value)
      val accessToken = sessionIdOpt.flatMap(cache.get[AccessToken])
      val sessionId = sessionIdOpt.getOrElse(UUID.randomUUID().toString)
      val result = block(TwitterLoginRequest(sessionId, accessToken, request))
      implicit val executionContext: ExecutionContext = cc.executionContext
      result.map(_.withCookies(Cookie(sessionIdName, sessionId, Some(30 * 60))))
    }

  }
}
