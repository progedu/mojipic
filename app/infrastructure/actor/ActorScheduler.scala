package infrastructure.actor

import javax.inject._

import akka.actor._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

@Singleton
class ActorScheduler @Inject()(@Named("convert-picture-actor") convertPictureActor: ActorRef,
                               system: ActorSystem) {

  system.scheduler.schedule(0 milliseconds, 500 milliseconds) {
    convertPictureActor ! ConvertPictureMessage
  }

}
