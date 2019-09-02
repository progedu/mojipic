package infrastructure.repository

import java.time.LocalDateTime

import com.google.common.net.MediaType
import domain.entity.{PictureId, PictureProperty, GitHubId}
import domain.repository.PicturePropertyRepository
import scalikejdbc._

import scala.concurrent.Future
import scala.util.Try

class PicturePropertyRepositoryImpl extends PicturePropertyRepository {

  def create(value: PictureProperty.Value): Future[PictureId] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""INSERT INTO picture_properties (
                 | status,
                 | twitter_id,
                 | file_name,
                 | content_type,
                 | overlay_text,
                 | overlay_text_size,
                 | original_filepath,
                 | converted_filepath,
                 | created_time
                 | ) VALUES (
                 | ${value.status.value},
                 | ${value.twitterId.value},
                 | ${value.fileName},
                 | ${value.contentType.toString},
                 | ${value.overlayText},
                 | ${value.overlayTextSize},
                 | ${value.originalFilepath.getOrElse(null)},
                 | ${value.convertedFilepath.getOrElse(null)},
                 | ${value.createdTime}
                 | )
              """.stripMargin
          PictureId(sql.updateAndReturnGeneratedKey().apply())
        }
      }
    })

  def find(pictureId: PictureId): Future[PictureProperty] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | picture_id,
                 | status,
                 | twitter_id,
                 | file_name,
                 | content_type,
                 | overlay_text,
                 | overlay_text_size,
                 | original_filepath,
                 | converted_filepath,
                 | created_time
                 | FROM picture_properties WHERE picture_id = ${pictureId.value}
              """.stripMargin
          sql.map(resultSetToPictureProperty).single().apply()
            .getOrElse(throw new RuntimeException(s"Picture is notfound. PictureId: ${pictureId.value}"))
        }
      }
    })

  private[this] def resultSetToPictureProperty(rs: WrappedResultSet): PictureProperty = {
    val value =
      PictureProperty.Value(
        PictureProperty.Status.parse(rs.string("status")).get,
        GitHubId(rs.long("twitter_id")),
        rs.string("file_name"),
        MediaType.parse(rs.string("content_type")),
        rs.string("overlay_text"),
        rs.int("overlay_text_size"),
        rs.stringOpt("original_filepath"),
        rs.stringOpt("converted_filepath"),
        rs.localDateTime("created_time")
      )
    PictureProperty(PictureId(rs.long("picture_id")), value)
  }

  def update(pictureId: PictureId, value: PictureProperty.Value): Future[Unit] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.localTx { implicit session =>
          val sql =
            sql"""UPDATE picture_properties SET
                 | status =  ${value.status.value},
                 | twitter_id = ${value.twitterId.value},
                 | file_name = ${value.fileName},
                 | content_type = ${value.contentType.toString},
                 | overlay_text = ${value.overlayText},
                 | overlay_text_size = ${value.overlayTextSize},
                 | original_filepath = ${value.originalFilepath.getOrElse("")},
                 | converted_filepath = ${value.convertedFilepath.getOrElse("")},
                 | created_time = ${value.createdTime}
                 | WHERE picture_id = ${pictureId.value}""".stripMargin
          sql.update().apply()
          ()
        }
      }
    })

  def findAllByTwitterIdAndDateTime(twitterId: GitHubId, toDateTime: LocalDateTime): Future[Seq[PictureProperty]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | picture_id,
                 | status,
                 | twitter_id,
                 | file_name,
                 | content_type,
                 | overlay_text,
                 | overlay_text_size,
                 | original_filepath,
                 | converted_filepath,
                 | created_time
                 | FROM picture_properties
                 | WHERE twitter_id = ${twitterId.value} AND created_time > ${toDateTime} ORDER BY created_time DESC
              """.stripMargin
          sql.map(resultSetToPictureProperty).list().apply()
        }
      }
    })

  def findAllByDateTime(toDateTime: LocalDateTime): Future[Seq[PictureProperty]] =
    Future.fromTry(Try {
      using(DB(ConnectionPool.borrow())) { db =>
        db.readOnly { implicit session =>
          val sql =
            sql"""SELECT
                 | picture_id,
                 | status,
                 | twitter_id,
                 | file_name,
                 | content_type,
                 | overlay_text,
                 | overlay_text_size,
                 | original_filepath,
                 | converted_filepath,
                 | created_time
                 | FROM picture_properties WHERE created_time > ${toDateTime} ORDER BY created_time DESC
              """.stripMargin
          sql.map(resultSetToPictureProperty).list().apply()
        }
      }
    })
}
