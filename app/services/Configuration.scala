package services

trait Configuration {
  def auth0_domain: String
  def auth0_client_id: String
  def auth0_client_secret: String
  def auth0_callback_url: String
  def auth0_logout_callback_url: String
}
