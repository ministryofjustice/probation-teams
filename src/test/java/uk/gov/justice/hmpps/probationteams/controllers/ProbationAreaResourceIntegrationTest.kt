package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.probationteams.utils.uniqueLduCode

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for ProbationAreaController")

class ProbationAreaResourceIntegrationTest(
  @Autowired val testRestTemplate: TestRestTemplate,
  @Autowired val entityBuilder: EntityWithJwtAuthorisationBuilder
) {

  val jsonTester = BasicJsonTester(this.javaClass)

  @Nested
  @DisplayName("GET $PROBATION_AREA_TEMPLATE")
  inner class GetProbationAreaTests {
    @Test
    fun `A Probation area that doesn't contain any FMBs`() {
      val response = getProbationArea("ZZZ")
      with(response) {
        assertThat(statusCode).isEqualTo(HttpStatus.OK)
        assertThat(jsonTester.from(body)).isEqualToJson("{ probationAreaCode: \"ZZZ\"}")
      }
    }

    @Test
    fun `A probation area that contains FMBs`() {
      val response = getProbationArea("ABC")
      with(response) {
        assertThat(statusCode).isEqualTo(HttpStatus.OK)
        assertThat(jsonTester.from(body)).isEqualToJson("probationArea.json")
      }
    }
  }

  @Nested
  @DisplayName("GET $LDU_TEMPLATE")
  inner class GetLduTests {
    @Test
    fun `An LDU that doesn't exist`() {
      val response = getLdu("ABC", "ABC123")
      with(response) {
        assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(body).isNullOrEmpty()
      }
    }

    @Test
    fun `An LDU with nested Probation Teams`() {
      val response = getLdu("ABC", "ABC125")
      with(response) {
        assertThat(statusCode).isEqualTo(HttpStatus.OK)
        assertThat(jsonTester.from(body)).isEqualToJson("lduDto2WithTeams.json")
      }
    }

    @Test
    fun `An LDU that exists`() {
      val response = getLdu("ABC", "ABC124")
      with(response) {
        assertThat(statusCode).isEqualTo(HttpStatus.OK)
        assertThat(jsonTester.from(body)).isEqualToJson("lduDto2.json")
      }
    }
  }

  @Nested
  @DisplayName("PUT $LDU_FMB_TEMPLATE")
  inner class PutFmbOnLdu {

    fun authorisedRolesProvider() = listOf(SYSTEM_USER_ROLE, MAINTAIN_REF_DATA_ROLE)

    @ParameterizedTest
    @MethodSource("authorisedRolesProvider")
    fun `Add a functional mailbox to an LDU`(roles: List<String>) {
      val lduCode = uniqueLduCode()

      assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)

      val putResponse = putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, roles)
      assertThat(putResponse.statusCode).isEqualTo(HttpStatus.CREATED)

      val response = getLdu(PROBATION_AREA_CODE, lduCode)
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      val content = jsonTester.from(response.body)
      assertThat(content).extractingJsonPathStringValue("$.probationAreaCode").isEqualTo(PROBATION_AREA_CODE)
      assertThat(content).extractingJsonPathStringValue("$.localDeliveryUnitCode").isEqualTo(lduCode)
      assertThat(content).extractingJsonPathStringValue("$.functionalMailbox").isEqualTo(FMB1)
    }

    @Test
    fun `Update a functional mailbox on an LDU`() {
      val lduCode = uniqueLduCode()

      putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, SYSTEM_USER_ROLE)

      assertThat(putLduFmb(PROBATION_AREA_CODE, lduCode, FMB2, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)

      val response = getLdu(PROBATION_AREA_CODE, lduCode)
      val content = jsonTester.from(response.body)
      assertThat(content).extractingJsonPathStringValue("$.functionalMailbox").isEqualTo(FMB2)
    }

    @Test
    fun `Operation is rejected when client does not have an authorised role`() {
      val response = putLduFmb(PROBATION_AREA_CODE, uniqueLduCode(), FMB1, NO_ROLES)
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
  }

  @Nested
  @DisplayName("DELETE $LDU_FMB_TEMPLATE")
  inner class DeleteFmbOnLdu {

    fun authorisedRolesProvider() = listOf(SYSTEM_USER_ROLE, MAINTAIN_REF_DATA_ROLE)

    @ParameterizedTest
    @MethodSource("authorisedRolesProvider")
    fun `delete an FMB`(roles: List<String>) {
      val lduCode = uniqueLduCode()

      putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, roles)
      assertThat(deleteLduFmb(PROBATION_AREA_CODE, lduCode, roles).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
      assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(deleteLduFmb(PROBATION_AREA_CODE, lduCode, roles).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Delete an FMB is refused when client does not have an authorised role`() {
      assertThat(deleteLduFmb(PROBATION_AREA_CODE, uniqueLduCode(), NO_ROLES).statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
  }

  @Nested
  @DisplayName("PUT $TEAM_FMB_TEMPLATE")
  inner class PutTeamFmb {
    fun authorisedRolesProvider() = listOf(SYSTEM_USER_ROLE, MAINTAIN_REF_DATA_ROLE)

    @ParameterizedTest
    @MethodSource("authorisedRolesProvider")
    fun `Create a new FMB for a team`(roles: List<String>) {
      val lduCode = uniqueLduCode()

      assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, roles).statusCode).isEqualTo(HttpStatus.CREATED)

      val content = jsonTester.from(getLdu(PROBATION_AREA_CODE, lduCode).body)
      assertThat(content).extractingJsonPathStringValue("$.probationTeams.$TEAM_1_CODE.functionalMailbox").isEqualTo(FMB1)
    }

    @Test
    fun `Update an FMB for a team`() {
      val lduCode = uniqueLduCode()

      assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.CREATED)
      assertThat(putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB2, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)

      val content = jsonTester.from(getLdu(PROBATION_AREA_CODE, lduCode).body)
      assertThat(content).extractingJsonPathStringValue("$.probationTeams.$TEAM_1_CODE.functionalMailbox").isEqualTo(FMB2)
    }

    @Test
    fun `Operation is rejected when client does not have an authorised role`() {
      val response = putTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), TEAM_1_CODE, FMB1, NO_ROLES)
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
  }

  @Nested
  @DisplayName("DELETE $TEAM_FMB_TEMPLATE")
  inner class DeleteTeamFmb {

    fun authorisedRolesProvider() = listOf(SYSTEM_USER_ROLE, MAINTAIN_REF_DATA_ROLE)

    @Test
    fun `LDU not found`() {
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Team not found`() {
      val lduCode = uniqueLduCode()
      putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, SYSTEM_USER_ROLE)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @ParameterizedTest
    @MethodSource("authorisedRolesProvider")
    fun `Team has FMB, parent LDU has one Team, no FMB`(roles: List<String>) {
      val lduCode = uniqueLduCode()
      putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, roles)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, roles).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, roles).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `Request is rejected when client does not have an authorised role`() {
      val lduCode = uniqueLduCode()
      putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, NO_ROLES).statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `Team has FMB, parent LDU has two Teams, no FMB`() {
      val lduCode = uniqueLduCode()
      putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE)
      putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_2_CODE, FMB2, SYSTEM_USER_ROLE)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `Team has FMB, parent LDU has one team and FMB`() {
      val lduCode = uniqueLduCode()
      putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE)
      putLduFmb(PROBATION_AREA_CODE, lduCode, FMB2, SYSTEM_USER_ROLE)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
      assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.OK)
    }
  }

  @Nested
  @DisplayName("Validation tests")
  inner class ValidationTests {

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("testData")
    fun validationTest(invocationWithTestData: InvocationWithTestData) = with(invocationWithTestData) {
      val response = describedInvocation.restApiInvocation(testData.code)
      assertThat(response.statusCode).isEqualTo(testData.expectedStatusCode)
      if (testData.expectedMessage != null) {
        val content = jsonTester.from(response.body)
        assertThat(content).extractingJsonPathStringValue("$.developerMessage").endsWith(testData.expectedMessage)
      }
    }

    /**
     * Build the combinations of RestApiInvocation and ValidationTestData to feed to the validationTest function above.
     * See 'times' operator defined below
     */
    fun testData(): List<InvocationWithTestData> = (
      probationAreaCodeConsumers * invalidProbationAreaCodes +
        lduCodeConsumers1 * invalidLocalDeliveryUnitCodes1 +
        lduCodeConsumers2 * invalidLocalDeliveryUnitCodes2 +
        teamCodeConsumers * invalidTeamCodes +
        emailAddressConsumers * invalidEmailAddresses
      ).map { InvocationWithTestData(it.first, it.second) }

    val probationAreaCodeConsumers = listOf(
      DescribedRestApiInvocation("PUT    LDU  FMB, try Probation Area Code") { putLduFmb(it, uniqueLduCode(), FMB1, SYSTEM_USER_ROLE) },
      DescribedRestApiInvocation("DELETE LDU  FMB, try Probation Area Code") { deleteLduFmb(it, uniqueLduCode(), SYSTEM_USER_ROLE) },
      DescribedRestApiInvocation("PUT    Team FMB, try Probation Area Code") { putTeamFmb(it, uniqueLduCode(), TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE) },
      DescribedRestApiInvocation("DELETE Team FMB, try Probation Area Code") { deleteTeamFmb(it, uniqueLduCode(), TEAM_1_CODE, SYSTEM_USER_ROLE) }
    )

    val lduCodeConsumers1 = listOf(
      DescribedRestApiInvocation("PUT    LDU  FMB, try LDU code") { putLduFmb(PROBATION_AREA_CODE, it, FMB1, SYSTEM_USER_ROLE) },
      DescribedRestApiInvocation("DELETE LDU  FMB, try LDU code") { deleteLduFmb(PROBATION_AREA_CODE, it, SYSTEM_USER_ROLE) }
    )

    val lduCodeConsumers2 = listOf(
      DescribedRestApiInvocation("PUT    Team FMB, try LDU code") { putTeamFmb(PROBATION_AREA_CODE, it, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE) },
      DescribedRestApiInvocation("PUT    Team FMB, try LDU code") { deleteTeamFmb(PROBATION_AREA_CODE, it, TEAM_1_CODE, SYSTEM_USER_ROLE) }
    )

    val teamCodeConsumers = listOf(
      DescribedRestApiInvocation("PUT    Team FMB, try Team code") { putTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), it, FMB1, SYSTEM_USER_ROLE) },
      DescribedRestApiInvocation("PUT    Team FMB, try Team code") { deleteTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), it, SYSTEM_USER_ROLE) }
    )

    val emailAddressConsumers = listOf(
      DescribedRestApiInvocation("PUT    LDU  FMB, try email address") { putLduFmb(PROBATION_AREA_CODE, uniqueLduCode(), it, SYSTEM_USER_ROLE) },
      DescribedRestApiInvocation("PUT    Team FMB, try email address") { putTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), TEAM_1_CODE, it, SYSTEM_USER_ROLE) }
    )

    val invalidProbationAreaCodes = listOf(
      ValidationTestData("a", HttpStatus.BAD_REQUEST, INVALID_PROBATION_AREA_MESSAGE),
      ValidationTestData("-", HttpStatus.BAD_REQUEST, INVALID_PROBATION_AREA_MESSAGE),
      ValidationTestData(" ", HttpStatus.BAD_REQUEST, INVALID_PROBATION_AREA_MESSAGE),
      ValidationTestData("", HttpStatus.UNAUTHORIZED, null)
    )

    val invalidLocalDeliveryUnitCodes2 = listOf(
      ValidationTestData("a", HttpStatus.BAD_REQUEST, INVALID_LDU_MESSAGE),
      ValidationTestData("-", HttpStatus.BAD_REQUEST, INVALID_LDU_MESSAGE),
      ValidationTestData(" ", HttpStatus.BAD_REQUEST, INVALID_LDU_MESSAGE),
      ValidationTestData("", HttpStatus.UNAUTHORIZED, null)
    )

    val invalidLocalDeliveryUnitCodes1 = invalidLocalDeliveryUnitCodes2.map(::adaptTestData)

    val invalidTeamCodes = listOf(
      ValidationTestData("a", HttpStatus.BAD_REQUEST, INVALID_TEAM_CODE_MESSAGE),
      ValidationTestData("-", HttpStatus.BAD_REQUEST, INVALID_TEAM_CODE_MESSAGE),
      ValidationTestData(" ", HttpStatus.BAD_REQUEST, INVALID_TEAM_CODE_MESSAGE),
      ValidationTestData("", HttpStatus.UNAUTHORIZED, null)
    )

    val invalidEmailAddresses = listOf(
      ValidationTestData("abc.def.com", HttpStatus.BAD_REQUEST, INVALID_EMAIL_MESSAGE),
      ValidationTestData(" ", HttpStatus.BAD_REQUEST, INVALID_EMAIL_MESSAGE),
      ValidationTestData("", HttpStatus.BAD_REQUEST, INVALID_EMAIL_MESSAGE)
    )
  }

  fun getProbationArea(probationAreaCode: String): ResponseEntity<String> =
    testRestTemplate.exchange(
      PROBATION_AREA_TEMPLATE,
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(A_USER, NO_ROLES),
      String::class.java,
      probationAreaCode
    )

  fun getLdu(probationAreaCode: String, lduCode: String): ResponseEntity<String> =
    testRestTemplate.exchange(
      LDU_TEMPLATE,
      HttpMethod.GET,
      entityBuilder.entityWithJwtAuthorisation(A_USER, NO_ROLES),
      String::class.java,
      probationAreaCode,
      lduCode
    )

  fun putLduFmb(probationAreaCode: String, lduCode: String, functionalMailbox: String, roles: List<String>): ResponseEntity<String> =
    testRestTemplate.exchange(
      LDU_FMB_TEMPLATE,
      HttpMethod.PUT,
      entityBuilder.entityWithJwtAuthorisation(A_USER, roles, "\"${functionalMailbox}\""),
      String::class.java,
      probationAreaCode,
      lduCode
    )

  fun putTeamFmb(probationAreaCode: String, lduCode: String, teamCode: String, functionalMailbox: String, roles: List<String>): ResponseEntity<String> =
    testRestTemplate.exchange(
      TEAM_FMB_TEMPLATE,
      HttpMethod.PUT,
      entityBuilder.entityWithJwtAuthorisation(A_USER, roles, "\"${functionalMailbox}\""),
      String::class.java,
      probationAreaCode,
      lduCode,
      teamCode
    )

  fun deleteLduFmb(probationAreaCode: String, lduCode: String, roles: List<String>): ResponseEntity<String> =
    testRestTemplate.exchange(
      LDU_FMB_TEMPLATE,
      HttpMethod.DELETE,
      entityBuilder.entityWithJwtAuthorisation(A_USER, roles),
      String::class.java,
      probationAreaCode,
      lduCode
    )

  fun deleteTeamFmb(probationAreaCode: String, lduCode: String, teamCode: String, roles: List<String>): ResponseEntity<String> =
    testRestTemplate.exchange(
      TEAM_FMB_TEMPLATE,
      HttpMethod.DELETE,
      entityBuilder.entityWithJwtAuthorisation(A_USER, roles),
      String::class.java,
      probationAreaCode,
      lduCode,
      teamCode
    )

  companion object {
    private const val PROBATION_AREA_TEMPLATE = "/probation-areas/{probationAreaCode}"
    private const val LDU_TEMPLATE = "/probation-areas/{probationAreaCode}/local-delivery-units/{lduCode}"
    private const val LDU_FMB_TEMPLATE = "/probation-areas/{probationAreaCode}/local-delivery-units/{lduCode}/functional-mailbox"

    private const val TEAM_FMB_TEMPLATE = "/probation-areas/{probationAreaCode}/local-delivery-units/{lduCode}/teams/{teamCode}/functional-mailbox"
    private const val PROBATION_AREA_CODE = "ABC"
    private const val TEAM_1_CODE = "T1"
    private const val TEAM_2_CODE = "T2"
    private const val FMB1 = "abc@def.com"

    private const val FMB2 = "pqr@stu.org"

    private const val A_USER = "API_TEST_USER"
    private val NO_ROLES = listOf<String>()
    private val MAINTAIN_REF_DATA_ROLE = listOf("ROLE_MAINTAIN_REF_DATA")

    private val SYSTEM_USER_ROLE = listOf("ROLE_SYSTEM_USER")

    private const val INVALID_PROBATION_AREA_MESSAGE = "Must be a valid Probation Area Code"
    private const val INVALID_LDU_MESSAGE = "Must be a valid Local Delivery Unit Code"
    private const val INVALID_TEAM_CODE_MESSAGE = "Must be a valid Probation Team Code"
    private const val INVALID_EMAIL_MESSAGE = "Must be a valid email address"
  }
}

/**
 * Represents an invocation of one of the probation-teams REST API end-points>  All parameters but one have already been supplied.
 * The remaining parameter is passed to this function.
 */
typealias RestApiInvocation = (String) -> ResponseEntity<String>

data class ValidationTestData(val code: String, val expectedStatusCode: HttpStatus, val expectedMessage: String?)

data class DescribedRestApiInvocation(val description: String, val restApiInvocation: RestApiInvocation)

data class InvocationWithTestData(val describedInvocation: DescribedRestApiInvocation, val testData: ValidationTestData) {
  override fun toString(): String = "${describedInvocation.description} '${testData.code}', expect HTTP status ${testData.expectedStatusCode.name}"
}

fun adaptTestData(testData: ValidationTestData): ValidationTestData = when (testData.expectedStatusCode) {
  HttpStatus.NOT_FOUND -> testData.copy(expectedStatusCode = HttpStatus.METHOD_NOT_ALLOWED)
  else -> testData
}

/**
 * Binary 'times' operator that takes two Lists and returns their Cartesian product as a List<Pair>
 */
operator fun <T, U> List<T>.times(us: List<U>): List<Pair<T, U>> = this.flatMap { t -> us.map { u -> Pair(t, u) } }
