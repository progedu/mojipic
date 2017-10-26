import java.time.Clock

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.redis.RedisClient
import domain.repository.PicturePropertyRepository
import infrastructure.actor.{ActorScheduler, ConvertPictureActor}
import infrastructure.repository.PicturePropertyRepositoryImpl
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

class Module(environment: Environment,
             configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {

  val redisHost = configuration.get[String]("mojipic.redis.host")
  val redisPort = configuration.get[Int]("mojipic.redis.port")

  def configure() = {
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[PicturePropertyRepository]).to(classOf[PicturePropertyRepositoryImpl])
    bind(classOf[RedisClient]).toInstance(new RedisClient(redisHost, redisPort))
    bindActor[ConvertPictureActor]("convert-picture-actor")
    bind(classOf[ActorScheduler])
      .annotatedWith(Names.named("actor-scheduler"))
      .to(classOf[ActorScheduler])
      .asEagerSingleton()
  }
}
