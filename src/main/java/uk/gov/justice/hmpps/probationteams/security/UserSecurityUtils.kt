package uk.gov.justice.hmpps.probationteams.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class UserSecurityUtils : AuthenticationFacade {
  val authentication: Authentication
    get() = SecurityContextHolder.getContext().authentication

  override fun getCurrentUsername(): String? {
    val username: String?
    val userPrincipal = userPrincipal
    if (userPrincipal is String) {
      username = userPrincipal
    } else if (userPrincipal is UserDetails) {
      username = userPrincipal.username
    } else if (userPrincipal is Map<*, *>) {
      username = (userPrincipal["username"] as String?)!!
    } else {
      username = null
    }
    return username
  }

  private val userPrincipal: Any?
    get() {
      var userPrincipal: Any? = null
      val auth = authentication
      if (auth != null) {
        userPrincipal = auth.principal
      }
      return userPrincipal
    }
}
