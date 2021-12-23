package auth

import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader
import play.mvc.Http.HeaderNames
import services.Configuration

import javax.inject.Inject
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait UserResolver {
  def resolve(request: RequestHeader): Option[UserProfile]
}

class UserResolverImpl @Inject() (
    sessionAccessor: SessionAccessor,
    ws: WSClient,
    configurations: Configuration
) extends UserResolver {
  override def resolve(request: RequestHeader): Option[UserProfile] = {
    // セッションにアクセストークンがあればユーザープロフィールを取得する
    sessionAccessor.getAccessToken(request).flatMap { accessToken =>
      // https://auth0.com/docs/api/authentication#database-ad-ldap-active-
      val request = ws.url(s"https://${configurations.auth0_domain}/userinfo")
        .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $accessToken")
        .get()

      scala.util.control.Exception.allCatch.opt(Await.result(request, 30.seconds)).flatMap { response =>
        response.json.validate(UserProfile.reads).asOpt
      }
    }
  }
}
