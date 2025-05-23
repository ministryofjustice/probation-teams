package uk.gov.justice.hmpps.probationteams.utils

object UserContext {
  private val authToken = ThreadLocal<String>()
  fun getAuthToken(): String = authToken.get()

  fun setAuthToken(aToken: String?) {
    authToken.set(aToken)
  }
}
