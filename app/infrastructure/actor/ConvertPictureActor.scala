package infrastructure.actor


import java.nio.file.{FileSystems, Files, Path}

import akka.actor.Actor
import com.google.inject.Inject
import com.redis.RedisClient
import domain.entity.{PictureId, PictureProperty}
import domain.repository.PicturePropertyRepository
import infrastructure.redis.RedisKeys
import org.im4java.core.{ConvertCmd, IMOperation}
import play.Logger
import play.api.Configuration

import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

sealed abstract trait ConvertPictureActorMessage

case object ConvertPictureMessage extends ConvertPictureActorMessage

class ConvertPictureActor @Inject()(
                                     redisClient: RedisClient,
                                     picturePropertyRepository: PicturePropertyRepository,
                                     configuration: Configuration
                                   ) extends Actor {

  val convertStoreDirPath = "./filesystem/converted"
  val storeDirPath = FileSystems.getDefault.getPath(convertStoreDirPath)
  if (!Files.exists(storeDirPath)) Files.createDirectories(storeDirPath)

  val imageMagickPath = configuration.get[String]("imagemagick.path")
  val imageMagickFontPath = configuration.get[String]("imagemagick.fontpath")

  override def receive = {

    case ConvertPictureMessage => {
      val stringId = redisClient.lpop[String](RedisKeys.Tasks)
      val pictureId = stringId.map(id => PictureId(id.toLong))
      val pictureProperty = pictureId.map(id => picturePropertyRepository.find(id))
      pictureProperty match {
        case Some(f) => f.foreach(p => convert(p, picturePropertyRepository))
        case None => // ignore Logger.info("Tasks queue is empty.")
      }
    }
  }

  private[this] def convert(pictureProperty: PictureProperty,
                            picturePropertyRepository: PicturePropertyRepository): Unit = {

    val filepath = FileSystems.getDefault.getPath(storeDirPath.toString, System.currentTimeMillis().toString)
    val convertedPictureProperty: PictureProperty = Try {
      invokeCmd(pictureProperty, filepath)
    } match {
      case Success(_) => PictureProperty(pictureProperty.id,
        pictureProperty.value
          .copy(status = PictureProperty.Status.Success,
            convertedFilepath = Some(filepath.toString))
      )
      case Failure(t) => {
        Logger.error("Fail to convert.", t)
        PictureProperty(pictureProperty.id, pictureProperty.value.copy(status = PictureProperty.Status.Failure))
      }
    }

    picturePropertyRepository.update(
      convertedPictureProperty.id,
      convertedPictureProperty.value
    ).onComplete {
      case Success(_) => Logger.info(s"Converted and updated. convertedPictureProperty: ${convertedPictureProperty}")
      case Failure(t) => Logger.error("Fail to update.", t)
    }
  }

  private[this] def invokeCmd(property: PictureProperty, convertedFilepath: Path): Unit = {
    val cmd = new ConvertCmd()
    cmd.setSearchPath(imageMagickPath)
    val op = new IMOperation()
    op.addImage(property.value.originalFilepath.getOrElse(""))
    op.gravity("south")
    op.font(imageMagickFontPath)
    op.pointsize(property.value.overlayTextSize)
    op.stroke("#000C")
    op.strokewidth(2)
    op.annotate(0, 0, 0, 0, property.value.overlayText)
    op.stroke("none")
    op.fill("white")
    op.annotate(0, 0, 0, 0, property.value.overlayText)
    op.addImage(convertedFilepath.toAbsolutePath.toString)
    cmd.run(op)
  }

}
