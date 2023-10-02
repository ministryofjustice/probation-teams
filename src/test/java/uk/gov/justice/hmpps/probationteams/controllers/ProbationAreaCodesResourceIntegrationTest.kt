package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for ProbationAreaCodesController")
class ProbationAreaCodesResourceIntegrationTest(
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
) {
  val jsonTester = BasicJsonTester(this.javaClass)

  @Nested
  @DisplayName("GET $PROBATION_AREA_CODES_TEMPLATE")
  inner class GetProbationAreaCodesTests {
    @Test
    fun `It returns all the probation area codes`() {
      val response = getProbationAreaCodes(SYSTEM_USER_ROLE)
      with(response) {
        assertThat(statusCode).isEqualTo(HttpStatus.OK)
        assertThat(jsonTester.from(body)).hasJsonPathArrayValue("$")
      }
    }
  }

  fun getProbationAreaCodes(roles: List<String>): ResponseEntity<String> =
    testRestTemplate.exchange(
      PROBATION_AREA_CODES_TEMPLATE,
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(A_USER, roles),
      String::class.java,
    )

  companion object {
    private const val PROBATION_AREA_CODES_TEMPLATE = "/probation-area-codes"

    private const val A_USER = "API_TEST_USER"
    private val SYSTEM_USER_ROLE = listOf("ROLE_SYSTEM_USER")
  }
}
