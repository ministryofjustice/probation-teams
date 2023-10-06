package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
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
@DisplayName("Integration Tests for LocalDeliveryUnitController")
class LocalDeliveryUnitControllerResourceIntegrationTest(
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder,
) {
  val jsonTester = BasicJsonTester(this.javaClass)

  @Test
  fun `All LDUs`() {
    val response = getLocalDeliveryUnits(VIEW_PROBATION_TEAMS_ROLE)
    with(response) {
      assertThat(statusCode).isEqualTo(HttpStatus.OK)

      assertThat(jsonTester.from(response.body))
        .extractingJsonPathArrayValue<String>("$.[*].probationTeams.[*].functionalMailbox")
        .contains("t1@b.com", "t2@b.com", "t3@b.com")
    }
  }

  fun getLocalDeliveryUnits(roles: List<String>): ResponseEntity<String> =
    testRestTemplate.exchange(
      ALL_LDU_TEMPLATE,
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(A_USER, roles),
      String::class.java,
    )

  companion object {
    private const val ALL_LDU_TEMPLATE = "/local-delivery-units"

    private const val A_USER = "API_TEST_USER"
    private val VIEW_PROBATION_TEAMS_ROLE = listOf("ROLE_VIEW_PROBATION_TEAMS")
  }
}
