package auth

import play.api.libs.json.Json

case class UserProfile(
    sub: String,
    nickname: String,
    name: String,
    picture: String,
    updated_at: String,
    email: String,
    email_verified: Boolean
)

object UserProfile {
  val reads = Json.reads[UserProfile]
}