package utils

import java.security.{ MessageDigest, SecureRandom }

object TokenGenerator {
  private val random = new SecureRandom()

  def gen(seed: String) = {
    val md = MessageDigest.getInstance("SHA-1")
    val sha1Digest  = md.digest((seed + random.nextInt()).getBytes("UTF-8"))
    sha1Digest.map("%02x".format(_)).mkString
  }
}
