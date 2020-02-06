package uk.gov.justice.hmpps.probationteams.config

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter

class UserIdAuthenticationConverter : DefaultUserAuthenticationConverter() {

    override fun extractAuthentication(map: Map<String?, *>): Authentication? =
            with(super.extractAuthentication(map)) {
                when (this) {
                    null -> null
                    else -> UsernamePasswordAuthenticationToken(
                            UserIdUser(
                                    name,
                                    credentials.toString(),
                                    authorities,
                                    map["user_id"] as String?),
                            credentials,
                            authorities)
                }
            }
}

class UserIdUser(
        username: String?,
        password: String?,
        authorities: Collection<GrantedAuthority?>?,
        val userId: String?) : User(username, password, authorities)
