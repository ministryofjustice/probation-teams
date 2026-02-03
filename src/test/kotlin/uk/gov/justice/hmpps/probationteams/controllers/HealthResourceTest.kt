package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.json.JsonContent
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@AutoConfigureTestRestTemplate
class HealthResourceTest {

  @Autowired lateinit var testRestTemplate: TestRestTemplate

  val jsonTester = BasicJsonTester(this.javaClass)

  @Test
  fun `Ping test`() {
    val response = testRestTemplate.getForEntity(PING_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(getContent(response)).hasJsonPathStringValue("$.status", "UP")
  }

  @Test
  fun `Health test`() {
    val response = testRestTemplate.getForEntity(HEALTH_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(getContent(response)).hasJsonPathStringValue("$.status", "UP")
    assertThat(getContent(response)).hasJsonPathStringValue("$.components.ping.status", "UP")
    assertThat(getContent(response)).hasJsonPathStringValue("$.components.db.status", "UP")
    assertThat(getContent(response)).hasJsonPathStringValue("$.components.diskSpace.status", "UP")
  }

  @Test
  fun `Info test`() {
    val response = testRestTemplate.getForEntity(INFO_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(getContent(response)).hasJsonPathStringValue("$.build.name", "probation-teams-api")
  }

  @Test
  fun `Health liveness page is accessible`() {
    val response = testRestTemplate.getForEntity(LIVENESS_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(getContent(response)).hasJsonPathStringValue("$.status", "UP")
  }

  @Test
  fun `Health readiness page is accessible`() {
    val response = testRestTemplate.getForEntity(READINESS_URL, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(getContent(response)).hasJsonPathStringValue("$.status", "UP")
  }

  private fun getContent(response: ResponseEntity<String>): JsonContent<in Any>? = jsonTester.from(response.body!!)

  companion object {
    private const val PING_URL = "/health/ping"
    private const val LIVENESS_URL = "/health/liveness"
    private const val READINESS_URL = "/health/readiness"
    private const val HEALTH_URL = "/health"
    private const val INFO_URL = "/info"
  }
}
