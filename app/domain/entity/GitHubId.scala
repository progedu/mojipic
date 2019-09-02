package domain.entity

import play.api.libs.json.JsString
import play.api.libs.json.Writes

/**
  * 投稿者のGitHub ID
  *
  * @param value GitHub IDの値
  */
case class GitHubId(value: String)

object GitHubId {
  implicit val writes: Writes[GitHubId] = Writes(id => JsString(id.value.toString))
}
