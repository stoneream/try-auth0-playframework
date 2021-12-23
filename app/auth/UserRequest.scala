package auth

import play.api.mvc.{ Request, WrappedRequest }

case class UserRequest[A](userProfile: UserProfile, request: Request[A]) extends WrappedRequest[A](request)
