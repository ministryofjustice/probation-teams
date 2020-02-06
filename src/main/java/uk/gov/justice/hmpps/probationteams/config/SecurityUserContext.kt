package uk.gov.justice.hmpps.probationteams.config

import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class SecurityUserContext {

    val currentUsername: Optional<String>
        get() = optionalCurrentUser.map { it.username }

    private val optionalCurrentUser: Optional<UserIdUser>
        get() = authentication().flatMap { authentication ->
            userPrincipal(authentication).map { userPrincipal ->
                when (userPrincipal) {
                    is UserIdUser -> userPrincipal
                    else -> userIdUser(usernameFromPrincipal(userPrincipal), authentication)
                }
            }
        }

    companion object {
        private val log = LoggerFactory.getLogger(SecurityUserContext::class.java)

        private fun authentication(): Optional<Authentication> = Optional.ofNullable(SecurityContextHolder.getContext().authentication)

        private fun userPrincipal(authentication: Authentication) = Optional.ofNullable(authentication.principal)

        private fun usernameFromPrincipal(userPrincipal: Any): String? = when (userPrincipal) {
            is String -> userPrincipal
            is UserDetails -> userPrincipal.username
            is Map<*, *> -> userPrincipal["username"] as String?
            else -> userPrincipal.toString()
        }

        private fun userIdUser(username: String?, authentication: Authentication): UserIdUser? = when (username) {
            null -> null
            "" -> null
            "anonymousUser" -> null
            else -> {
                log.debug("Authentication doesn't contain user id, using username instead")

                UserIdUser(
                        username,
                        authentication.credentials?.toString(),
                        authentication.authorities,
                        username)
            }
        }
    }
}