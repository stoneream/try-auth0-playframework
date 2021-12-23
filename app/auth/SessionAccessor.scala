package auth

import play.api.mvc.{ RequestHeader, Result }

class SessionAccessor {
  private val sessionState = "state"
  private val sessionAccessToken = "access_token"

  def setAccessToken(accessToken: String)(result: Result) = result.withSession(sessionAccessToken -> accessToken)

  def getAccessToken(request: RequestHeader) = request.session.get(sessionAccessToken)

  def setState(state: String)(result: Result) = result.withSession(sessionState -> state)

  def getState(request: RequestHeader) = request.session.get(sessionState)
}
