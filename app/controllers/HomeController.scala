package controllers

import javax.inject._
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.Security
import play.api.cache.SyncCacheApi
import play.api.mvc._

import scala.compat.java8.OptionConverters._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val cache: SyncCacheApi,
                               cc: ControllerComponents) extends Security[CommonProfile] {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  private def isAuthenticated(implicit request: RequestHeader): Boolean = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    profileManager.isAuthenticated
  }

  private def getProfile(implicit request: RequestHeader): Option[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profile = profileManager.get(true)
    profile.asScala
  }

  def index() = Action { implicit request =>
    Ok(views.html.index(isAuthenticated(request), getProfile(request)))
  }
}
