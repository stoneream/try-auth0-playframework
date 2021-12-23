package controllers

import auth.{ SessionAccessor, UserAction }
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import play.mvc.Http.{ HeaderNames, MimeTypes }
import services.Configuration
import utils.TokenGenerator

import javax.inject.{ Inject, Singleton }
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@Singleton
class SessionController @Inject() (
    controllerComponents: ControllerComponents,
    userAction: UserAction,
    ws: WSClient,
    configurations: Configuration,
    sessionAccessor: SessionAccessor
) extends AbstractController(controllerComponents) {
  // ログイン（認可リクエスト画面へリダイレクトが発生するのみ）
  // stateはセッションに保持
  def login = userAction.optional { implicit request: Request[AnyContent] =>
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

    sessionAccessor.setState(state)(Redirect(url, param))
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

  // 認可レスポンスを受け取るエンドポイント
  def callback = Action { implicit request: Request[AnyContent] =>
    val redirect = Redirect("/")

    val paramsOpt = for {
      localState <- sessionAccessor.getState(request)
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
                  sessionAccessor.setAccessToken(valid.access_token)(redirect.withNewSession)
                }
              )
            case Left(error) =>
              redirect
          }
        } else {
          // stateが一致しない
          redirect
        }
    }
  }

  def logout = userAction.normal { implicit request =>
    // https://auth0.com/docs/login/logout
    // https://auth0.com/docs/api/authentication#logout
    val url = s"https://${configurations.auth0_domain}/v2/logout"
    val param = Map(
      "client_id" -> Seq(configurations.auth0_client_id),
      "returnTo" -> Seq(configurations.auth0_logout_callback_url)
    )

    sessionAccessor.removeAccessToken(request)(Redirect(url, param))
  }
}
