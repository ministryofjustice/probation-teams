package uk.gov.justice.hmpps.probationteams.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class UserSecurityUtils : AuthenticationFacade {
  val authentication: Authentication?
    get() = SecurityContextHolder.getContext().authentication

  override fun getCurrentUsername(): String? = when (userPrincipal) {
    is String -> userPrincipal as String
    is UserDetails -> (userPrincipal as UserDetails).username
    is Map<*, *> -> (userPrincipal as Map<*, *>)["username"] as String?
    else -> null
  }

  private val userPrincipal: Any?
    get() = authentication?.principal
}
