package auth

import play.api.mvc.{ Request, WrappedRequest }

case class OptionalUserRequest[A](userProfile: Option[UserProfile], request: Request[A]) extends WrappedRequest[A](request)
