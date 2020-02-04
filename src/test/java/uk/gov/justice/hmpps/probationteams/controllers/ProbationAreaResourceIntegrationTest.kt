package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import org.junit.jupiter.params.provider.Arguments.arguments
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import uk.gov.justice.hmpps.probationteams.utils.JwtAuthenticationHelper
import uk.gov.justice.hmpps.probationteams.utils.uniqueLduCode

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["test"])
@DisplayName("Integration Tests for ProbationAreaController")


class ProbationAreaResourceIntegrationTest @Autowired constructor(
        val testRestTemplate: TestRestTemplate,
        jwtAuthenticationHelper: JwtAuthenticationHelper
) : ResourceTest(jwtAuthenticationHelper) {

    val jsonTester = BasicJsonTester(this.javaClass)

    @Nested
    @DisplayName("GET ${LDU_TEMPLATE}")
    inner class GetLduTests {
        @Test
        fun `An LDU that doesn't exist`() {
            val response = getLdu("ABC", "ABC123")
            assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(response.body).isNullOrEmpty()
        }

        @Test
        fun `An LDU with nested Probation Teams`() {
            val response = getLdu("ABC", "ABC125")
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(getBodyAsJsonContent<Any>(response)).isEqualToJson("lduDto2WithTeams.json")
        }

        @Test
        fun `An LDU that exists`() {
            val response = getLdu("ABC", "ABC124")
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(getBodyAsJsonContent<Any>(response)).isEqualToJson("lduDto2.json")
        }
    }

    @Nested
    @DisplayName("PUT ${LDU_FMB_TEMPLATE}")
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
        fun `Operation is rejected when requestor does not have an authorised role`() {
            val response = putLduFmb(PROBATION_AREA_CODE, uniqueLduCode(), FMB1, NO_ROLES)
            assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        }
    }

    @Nested
    @DisplayName("DELETE ${LDU_FMB_TEMPLATE}")
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
    @DisplayName("PUT ${TEAM_FMB_TEMPLATE}")
    inner class PutTeamFmb {
        fun authorisedRolesProvider() = listOf(SYSTEM_USER_ROLE, MAINTAIN_REF_DATA_ROLE)

        @ParameterizedTest
        @MethodSource("authorisedRolesProvider")
        fun `Create a new FMB for a team`(roles: List<String>) {
            val lduCode = uniqueLduCode()

            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, roles).statusCode).isEqualTo(HttpStatus.CREATED)

            val content = jsonTester.from(getLdu(PROBATION_AREA_CODE, lduCode).body)
            assertThat(content).extractingJsonPathStringValue("$.probationTeams.${TEAM_1_CODE}.functionalMailbox").isEqualTo(FMB1)
        }

        @Test
        fun `Update an FMB for a team`() {
            val lduCode = uniqueLduCode()

            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(putTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB2, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)

            val content = jsonTester.from(getLdu(PROBATION_AREA_CODE, lduCode).body)
            assertThat(content).extractingJsonPathStringValue("$.probationTeams.${TEAM_1_CODE}.functionalMailbox").isEqualTo(FMB2)
        }

        @Test
        fun `Operation is rejected when client does not have an authorised role`() {
            val response = putTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), TEAM_1_CODE, FMB1, NO_ROLES)
            assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        }
    }

    @Nested
    @DisplayName("DELETE ${TEAM_FMB_TEMPLATE}")
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

        fun doTest(validationTestData: ValidationTestData, restFunction: (String) -> ResponseEntity<String>): Unit {
            val response = restFunction(validationTestData.code)
            assertThat(response.statusCode).isEqualTo(validationTestData.expectedStatus)
            val content = jsonTester.from(response.body)
            if (validationTestData.expectedMessage != null)
                assertThat(content).extractingJsonPathStringValue("$.developerMessage").contains(validationTestData.expectedMessage)
        }

        @Nested
        @DisplayName("PUT ${LDU_FMB_TEMPLATE}")
        inner class PutLduFmb {

            @ParameterizedTest
            @ArgumentsSource(InvalidProbationAreaCodes::class)
            fun `Invalid probation area code`(validationTestData: ValidationTestData) = doTest(validationTestData) { putLduFmb(it, uniqueLduCode(), FMB1, SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidLocalDeliveryUnitCodes::class)
            fun `Invalid LDU code`(validationTestData: ValidationTestData) = doTest(adaptTestData(validationTestData)) { putLduFmb(PROBATION_AREA_CODE, it, FMB1, SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidEmailAddresses::class)
            fun `Invalid Functional Mailbox address`(validationTestData: ValidationTestData) = doTest(validationTestData) { putLduFmb(PROBATION_AREA_CODE, uniqueLduCode(), it, SYSTEM_USER_ROLE) }
        }

        @Nested
        @DisplayName("DELETE ${LDU_FMB_TEMPLATE}")
        inner class DeleteLduFmb {

            @ParameterizedTest
            @ArgumentsSource(InvalidProbationAreaCodes::class)
            fun `Invalid probation area code`(validationTestData: ValidationTestData) = doTest(validationTestData) { deleteLduFmb(it, uniqueLduCode(), SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidLocalDeliveryUnitCodes::class)
            fun `Invalid LDU code`(validationTestData: ValidationTestData) = doTest(adaptTestData(validationTestData)) { deleteLduFmb(PROBATION_AREA_CODE, it, SYSTEM_USER_ROLE) }
        }

        @Nested
        @DisplayName("PUT ${TEAM_FMB_TEMPLATE}")
        inner class PutTeamFmb {

            @ParameterizedTest
            @ArgumentsSource(InvalidProbationAreaCodes::class)
            fun `Invalid probation area code`(validationTestData: ValidationTestData) = doTest(validationTestData) { putTeamFmb(it, uniqueLduCode(), TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidLocalDeliveryUnitCodes::class)
            fun `Invalid LDU code`(validationTestData: ValidationTestData) = doTest(validationTestData) { putTeamFmb(PROBATION_AREA_CODE, it, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidTeamCodes::class)
            fun `Invalid Team code`(validationTestData: ValidationTestData) = doTest(validationTestData) { putTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), it, FMB1, SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidEmailAddresses::class)
            fun `Invalid Functional Mailbox address`(validationTestData: ValidationTestData) = doTest(validationTestData) { putTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), TEAM_1_CODE, it, SYSTEM_USER_ROLE) }
        }

        @Nested
        @DisplayName("DELETE ${TEAM_FMB_TEMPLATE}")
        inner class DeleteTeamFmb {

            @ParameterizedTest
            @ArgumentsSource(InvalidProbationAreaCodes::class)
            fun `Invalid probation area code`(validationTestData: ValidationTestData) = doTest(validationTestData) { deleteTeamFmb(it, uniqueLduCode(), TEAM_1_CODE, SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidLocalDeliveryUnitCodes::class)
            fun `Invalid LDU code`(validationTestData: ValidationTestData) = doTest(validationTestData) { deleteTeamFmb(PROBATION_AREA_CODE, it, TEAM_1_CODE, SYSTEM_USER_ROLE) }

            @ParameterizedTest
            @ArgumentsSource(InvalidTeamCodes::class)
            fun `Invalid Team code`(validationTestData: ValidationTestData) = doTest(validationTestData) { deleteTeamFmb(PROBATION_AREA_CODE, uniqueLduCode(), it, SYSTEM_USER_ROLE) }
        }
    }

    fun getLdu(probationAreaCode: String, lduCode: String): ResponseEntity<String> =
            testRestTemplate.exchange(
                    LDU_TEMPLATE,
                    HttpMethod.GET,
                    createHttpEntityWithBearerAuthorisation(A_USER, NO_ROLES),
                    String::class.java,
                    probationAreaCode, lduCode)

    fun putLduFmb(probationAreaCode: String, lduCode: String, functionalMailbox: String, roles: List<String>): ResponseEntity<String> =
            testRestTemplate.exchange(
                    LDU_FMB_TEMPLATE,
                    HttpMethod.PUT,
                    createHttpEntityWithBearerAuthorisation(A_USER, roles, "\"${functionalMailbox}\""),
                    String::class.java,
                    probationAreaCode, lduCode)

    fun putTeamFmb(probationAreaCode: String, lduCode: String, teamCode: String, functionalMailbox: String, roles: List<String>): ResponseEntity<String> =
            testRestTemplate.exchange(
                    TEAM_FMB_TEMPLATE,
                    HttpMethod.PUT,
                    createHttpEntityWithBearerAuthorisation(A_USER, roles, "\"${functionalMailbox}\""),
                    String::class.java,
                    probationAreaCode, lduCode, teamCode)

    fun deleteLduFmb(probationAreaCode: String, lduCode: String, roles: List<String>): ResponseEntity<String> =
            testRestTemplate.exchange(
                    LDU_FMB_TEMPLATE,
                    HttpMethod.DELETE,
                    createHttpEntityWithBearerAuthorisation(A_USER, roles),
                    String::class.java,
                    probationAreaCode, lduCode)

    fun deleteTeamFmb(probationAreaCode: String, lduCode: String, teamCode: String, roles: List<String>): ResponseEntity<String> =
            testRestTemplate.exchange(
                    TEAM_FMB_TEMPLATE,
                    HttpMethod.DELETE,
                    createHttpEntityWithBearerAuthorisation(A_USER, roles),
                    String::class.java,
                    probationAreaCode, lduCode, teamCode)

    companion object {
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
    }
}

data class ValidationTestData(val code: String, val expectedStatus: HttpStatus, val expectedMessage: String?)

fun adaptTestData(validationTestData: ValidationTestData): ValidationTestData = when (validationTestData.expectedStatus) {
    HttpStatus.NOT_FOUND -> validationTestData.copy(expectedStatus = HttpStatus.METHOD_NOT_ALLOWED)
    else -> validationTestData
}

class InvalidProbationAreaCodes : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?) =
            listOf(
                    arguments(ValidationTestData("a", HttpStatus.BAD_REQUEST, "probationAreaCode: Invalid Probation Area code")),
                    arguments(ValidationTestData("-", HttpStatus.BAD_REQUEST, "probationAreaCode: Invalid Probation Area code")),
                    arguments(ValidationTestData(" ", HttpStatus.BAD_REQUEST, "probationAreaCode: must not be blank")),
                    arguments(ValidationTestData("", HttpStatus.NOT_FOUND, null))
            ).stream()
}

class InvalidLocalDeliveryUnitCodes : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?) =
            listOf(
                    arguments(ValidationTestData("a", HttpStatus.BAD_REQUEST, "localDeliveryUnitCode: Invalid Local Delivery Unit code")),
                    arguments(ValidationTestData("-", HttpStatus.BAD_REQUEST, "localDeliveryUnitCode: Invalid Local Delivery Unit code")),
                    arguments(ValidationTestData(" ", HttpStatus.BAD_REQUEST, "localDeliveryUnitCode: must not be blank")),
                    arguments(ValidationTestData("", HttpStatus.NOT_FOUND, null))
            ).stream()
}

class InvalidTeamCodes : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?) =
            listOf(
                    arguments(ValidationTestData("a", HttpStatus.BAD_REQUEST, "teamCode: Invalid Team code")),
                    arguments(ValidationTestData("-", HttpStatus.BAD_REQUEST, "teamCode: Invalid Team code")),
                    arguments(ValidationTestData(" ", HttpStatus.BAD_REQUEST, "teamCode: must not be blank")),
                    arguments(ValidationTestData("", HttpStatus.NOT_FOUND, null))
            ).stream()
}

class InvalidEmailAddresses : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext?) =
            listOf(
                    arguments(ValidationTestData("abc.def.com", HttpStatus.BAD_REQUEST, "must be a well-formed email address")),
                    arguments(ValidationTestData(" ", HttpStatus.BAD_REQUEST, "must not be blank")),
                    arguments(ValidationTestData("", HttpStatus.BAD_REQUEST, "must not be blank"))
            ).stream()
}