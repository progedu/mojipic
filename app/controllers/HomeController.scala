package controllers

import javax.inject._

import play.api.cache.SyncCacheApi
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val cache: SyncCacheApi,
                               cc: ControllerComponents) extends TwitterLoginController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = TwitterLoginAction { implicit request: TwitterLoginRequest[AnyContent] =>
    Ok(views.html.index(request.accessToken))
  }
}
