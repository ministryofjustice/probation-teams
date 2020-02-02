package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions
import org.springframework.boot.test.json.JsonContent
import org.springframework.core.ResolvableType
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.probationteams.utils.JwtAuthenticationHelper
import uk.gov.justice.hmpps.probationteams.utils.JwtParameters
import java.time.Duration
import java.util.*


abstract class ResourceTest(private val jwtAuthenticationHelper: JwtAuthenticationHelper) {

    fun createHttpEntityWithBearerAuthorisation(user: String, roles: List<String>): HttpEntity<*> = HttpEntity<Any>(null, addCommonHeaders(user, roles))

    fun createHttpEntityWithBearerAuthorisation(user: String, roles: List<String>, body: Any): HttpEntity<*> {
        val headers = addCommonHeaders(user, roles)
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        return HttpEntity(body, headers)
    }

    private fun addCommonHeaders(user: String, roles: List<String>): HttpHeaders {
        val jwt = createJwt(user, roles)
        val headers = HttpHeaders()
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer $jwt")
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        return headers
    }

    fun <T> assertThatStatus(response: ResponseEntity<T>, status: Int) {
        Assertions
                .assertThat(response.statusCodeValue)
                .withFailMessage("Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s", response.statusCodeValue, status, response.body)
                .isEqualTo(status)
    }

    fun <T> assertThatJsonFileAndStatus(response: ResponseEntity<String>, status: Int, jsonFile: String?) {
        assertThatStatus(response, status)
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).isEqualToJson(jsonFile)
    }

    protected fun <T> getBodyAsJsonContent(response: ResponseEntity<String>): JsonContent<T> =
            JsonContent(javaClass, ResolvableType.forType(String::class.java), Objects.requireNonNull(response.body))


    fun createJwt(user: String, roles: List<String>): String =
            jwtAuthenticationHelper.createJwt(JwtParameters(
                    username = user,
                    userId = user + "_ID",
                    roles = roles,
                    scope = listOf("read", "write"),
                    expiryTime = Duration.ofDays(1)
            ))
}