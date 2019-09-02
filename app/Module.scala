import java.time.Clock

import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.Names
import com.redis.RedisClient
import domain.repository.PicturePropertyRepository
import infrastructure.actor.{ActorScheduler, ConvertPictureActor}
import infrastructure.repository.PicturePropertyRepositoryImpl
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.oauth.client.GitHubClient
import org.pac4j.play.http.PlayHttpActionAdapter
import org.pac4j.play.{CallbackController, LogoutController}
import org.pac4j.play.scala.{DefaultSecurityComponents, SecurityComponents}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

class Module(environment: Environment,
             configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {

  val base_url = configuration.get[String]("mojipic.documentrooturl")
  val redisHost = configuration.get[String]("mojipic.redis.host")
  val redisPort = configuration.get[Int]("mojipic.redis.port")

  override def configure() = {
    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])
    bind(classOf[SecurityComponents]).to(classOf[DefaultSecurityComponents])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/")
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)

    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[PicturePropertyRepository]).to(classOf[PicturePropertyRepositoryImpl])
    bind(classOf[RedisClient]).toInstance(new RedisClient(redisHost, redisPort))
    bindActor[ConvertPictureActor]("convert-picture-actor")
    bind(classOf[ActorScheduler])
      .annotatedWith(Names.named("actor-scheduler"))
      .to(classOf[ActorScheduler])
      .asEagerSingleton()
  }

  @Provides
  def provideGithubClient: GitHubClient = new GitHubClient(
    configuration.get[String]("mojipic.client_id"),
    configuration.get[String]("mojipic.client_secret")
  )

  @Provides
  def provideConfig(gitHubClient: GitHubClient): Config = {
    val clients = new Clients(base_url + "/oauth_callback", gitHubClient)
    val config = new Config(clients)
    config.setHttpActionAdapter(new PlayHttpActionAdapter())
    config
  }
}
