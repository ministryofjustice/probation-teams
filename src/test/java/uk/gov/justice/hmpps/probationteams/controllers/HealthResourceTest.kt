package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.probationteams.utils.JwtAuthenticationHelper

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)

class HealthResourceTest @Autowired constructor(
        val testRestTemplate: TestRestTemplate,
        jwtAuthenticationHelper: JwtAuthenticationHelper
) : ResourceTest(jwtAuthenticationHelper) {
    @Test
    fun `Ping test`() {
        val response = testRestTemplate.getForEntity(PING_URL, String::class.java)
        assertThatStatus(response, HttpStatus.OK.value())
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).hasJsonPathStringValue("$.status", "UP")
    }

    @Test
    fun `Health test`() {
        val response = testRestTemplate.getForEntity(HEALTH_URL, String::class.java)
        assertThatStatus(response, HttpStatus.OK.value())
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).hasJsonPathStringValue("$.status", "UP")
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).hasJsonPathStringValue("$.components.ping.status", "UP")
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).hasJsonPathStringValue("$.components.db.status", "UP")
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).hasJsonPathStringValue("$.components.diskSpace.status", "UP")
    }

    @Test
    fun `Info test`() {
        val response = testRestTemplate.getForEntity(INFO_URL, String::class.java)
        assertThatStatus(response, HttpStatus.OK.value())
        Assertions.assertThat(getBodyAsJsonContent<Any>(response)).hasJsonPathStringValue("$.test-message", "Info Test")
    }

    companion object {
        private const val PING_URL = "/health/ping"
        private const val HEALTH_URL = "/health"
        private const val INFO_URL = "/info"
    }
}