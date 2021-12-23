package auth

import play.api.mvc.{ ActionBuilder, AnyContent, BodyParsers, Request, RequestHeader, Result, WrappedRequest, Results }

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class UserAction @Inject() (
    userResolver: UserResolver,
    parser: BodyParsers.Default
)(implicit ec: ExecutionContext) {

  val normal = new UserActionBuilder(parser)

  val optional = new OptionalUserActionBuilder(parser)

  def failedUserResolveHandler(request: RequestHeader)(implicit ec: ExecutionContext): Future[Result] = {
    Future.successful(Results.Redirect("/login"))
  }

  class UserActionBuilder(val parser: BodyParsers.Default) extends ActionBuilder[UserRequest, AnyContent] {
    override protected def executionContext: ExecutionContext = ec

    override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
      userResolver.resolve(request).fold {
        failedUserResolveHandler(request)
      } { userProfile =>
        block(UserRequest(userProfile, request))
      }
    }
  }

  class OptionalUserActionBuilder(val parser: BodyParsers.Default) extends ActionBuilder[OptionalUserRequest, AnyContent] {
    override protected def executionContext: ExecutionContext = ec

    override def invokeBlock[A](request: Request[A], block: OptionalUserRequest[A] => Future[Result]): Future[Result] = {
      val userProfileOpt = userResolver.resolve(request)
      block(OptionalUserRequest(userProfileOpt, request))
    }
  }
}