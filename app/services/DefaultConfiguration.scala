package services

import javax.inject.{ Inject, Singleton }

@Singleton
class DefaultConfiguration @Inject() (configuration: play.api.Configuration) extends Configuration {
  private def orError(key: String): String = configuration.get[String](key)

  override def auth0_domain: String = orError("auth0.domain")

  override def auth0_client_id: String = orError("auth0.client.id")

  override def auth0_client_secret: String = orError("auth0.client.secret")

  override def auth0_callback_url: String = orError("auth0.callback.url")
}
