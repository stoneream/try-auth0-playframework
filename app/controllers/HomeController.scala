package controllers

import auth.UserAction
import play.api.mvc._

import javax.inject._

@Singleton
class HomeController @Inject() (
    userAction: UserAction,
    controllerComponents: ControllerComponents
) extends AbstractController(controllerComponents) {
  // トップ
  def index = userAction.optional { implicit request =>
    Ok(views.html.index(request.userProfile))
  }

  // プロフィール
  def profile = userAction.normal { implicit request =>
    Ok(views.html.profile(request.userProfile))
  }
}
