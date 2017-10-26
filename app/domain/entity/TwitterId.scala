package domain.entity

import play.api.libs.json.JsString
import play.api.libs.json.Writes

/**
  * 投稿者のTwitter ID
  *
  * @param value Twitter IDの値
  */
case class TwitterId(value: Long)

object TwitterId {
  implicit val writes: Writes[TwitterId] = Writes(id => JsString(id.value.toString))
}
