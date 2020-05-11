package uk.gov.justice.hmpps.probationteams.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.probationteams.utils.JwtAuthenticationHelper
import uk.gov.justice.hmpps.probationteams.utils.JwtParameters
import java.time.Duration

@Component
class EntityWithJwtAuthorisationBuilder(@Autowired val jwtAuthenticationHelper: JwtAuthenticationHelper) {

    fun entityWithJwtAuthorisation(user: String, roles: List<String>, body: Any): HttpEntity<*> {
        val headers = addCommonHeaders(user, roles)
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        return HttpEntity(body, headers)
    }

    fun entityWithJwtAuthorisation(user: String, roles: List<String>): HttpEntity<*> = HttpEntity<Any>(null, addCommonHeaders(user, roles))

    private fun addCommonHeaders(user: String, roles: List<String>): HttpHeaders {
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ${createJwt(user, roles)}")
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        return headers
    }

    fun createJwt(user: String, roles: List<String>): String =
            jwtAuthenticationHelper.createJwt(
                            subject = user,
                            roles = roles,
                            scope = listOf("read", "write"),
                            expiryTime = Duration.ofDays(1)
                    )
}