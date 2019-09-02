package controllers

import javax.inject.{Inject, Singleton}
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala.{Security, SecurityComponents}
import play.api.mvc.Session

import scala.collection.JavaConverters._

@Singleton
class OAuthController @Inject() (val controllerComponents: SecurityComponents) extends Security[CommonProfile] {
  def login = Action { request =>
    val context: PlayWebContext = new PlayWebContext(request, playSessionStore)
    val client = config.getClients.findClient("GitHubClient").asInstanceOf[IndirectClient[Credentials,CommonProfile]]
    val location = client.getRedirectAction(context).getLocation
    val newSession = new Session(mapAsScalaMap(context.getJavaSession).toMap)
    Redirect(location).withSession(newSession)
  }
}
