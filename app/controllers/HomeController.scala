package controllers

import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.mvc.Http.{ HeaderNames, MimeTypes }
import utils.TokenGenerator

import javax.inject._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@Singleton
class HomeController @Inject() (
    controllerComponents: ControllerComponents,
    configurations: services.Configuration,
    ws: WSClient
) extends AbstractController(controllerComponents) {
  // トップページ
  // セッションにアクセストークンがあればユーザープロフィールを取得する
  def index() = Action { implicit request: Request[AnyContent] =>
    // resolve user
    val accessTokenOpt = request.session.get("access_token")
    val userProfileOpt = accessTokenOpt.flatMap { accessToken =>
      // https://auth0.com/docs/api/authentication#database-ad-ldap-active-
      val request = ws.url(s"https://${configurations.auth0_domain}/userinfo")
        .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $accessToken")
        .get()

      scala.util.control.Exception.allCatch.opt(Await.result(request, 30.seconds)).flatMap { response =>
        response.json.validate(UserProfile.reads).asOpt
      }
    }

    Ok(views.html.index(userProfileOpt))
  }

  // ログイン（認可リクエスト画面へリダイレクトが発生するのみ）
  // stateはセッションに保持
  def login = Action { implicit request: Request[AnyContent] =>
    val now = DateTime.now()
    val state = TokenGenerator.gen(now.toString)

    // 認可リクエスト
    // https://auth0.com/docs/login/authentication/add-login-auth-code-flow#authorization-url-example
    val url = s"https://${configurations.auth0_domain}/authorize"
    val param = Map(
      "response_type" -> Seq("code"),
      "client_id" -> Seq(configurations.auth0_client_id),
      "redirect_uri" -> Seq(configurations.auth0_callback_url),
      "state" -> Seq(state),
      "scope" -> Seq("email openid profile")
    )

    Redirect(url, param).withSession("state" -> state)
  }

  // 認可レスポンスを受け取るエンドポイント
  def callback = Action { implicit request: Request[AnyContent] =>
    val redirect = Redirect("/").removingFromSession("state")

    val paramsOpt = for {
      localState <- request.session.get("state")
      state <- request.getQueryString("state")
      code <- request.getQueryString("code")
    } yield (localState, state, code)

    paramsOpt.fold {
      // 必要なパラメーターがない
      redirect
    } {
      case (localState, state, code) =>
        if (localState == state) {
          // アクセストークンをリクエスト
          // https://auth0.com/docs/login/authentication/add-login-auth-code-flow#request-tokens
          val request = ws.url(s"https://${configurations.auth0_domain}/oauth/token")
            .withHttpHeaders(HeaderNames.CONTENT_TYPE -> MimeTypes.FORM)
            .post(Map(
              "grant_type" -> "authorization_code",
              "client_id" -> configurations.auth0_client_id,
              "client_secret" -> configurations.auth0_client_secret,
              "code" -> code,
              "redirect_uri" -> configurations.auth0_callback_url
            ))

          scala.util.control.Exception.allCatch.either(Await.result(request, 30.seconds)) match {
            case Right(value) =>
              value.json.validate(AuthorizationRequestResponse.reads).fold(
                invalid => {
                  // 不明なレスポンスタイプ
                  redirect
                },
                valid => {
                  // とりあえずアクセストークン・IDトークンをCookieにセット
                  redirect
                    .withNewSession
                    .withSession(
                      "access_token" -> valid.access_token,
                      "id_token" -> valid.id_token
                    )
                }
              )
            case Left(error) =>
              println(error.getMessage)
              redirect
          }
        } else {
          // stateが一致しない
          redirect
        }
    }
  }
}

case class AuthorizationRequestResponse(
    access_token: String,
    id_token: String,
    scope: String,
    expires_in: Int,
    token_type: String
)

object AuthorizationRequestResponse {
  val reads = Json.reads[AuthorizationRequestResponse]
}

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