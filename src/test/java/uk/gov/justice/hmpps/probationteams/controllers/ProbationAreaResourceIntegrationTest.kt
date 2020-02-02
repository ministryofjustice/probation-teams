package uk.gov.justice.hmpps.probationteams.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
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

        @Test
        fun `Add a functional mailbox to an LDU`() {
            val lduCode = uniqueLduCode()

            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)

            val putResponse = putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, SYSTEM_USER_ROLE)
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
        fun `Operation is rejected when requestor does not have SYSTEM_USER or MAINTAIN_REF_DATA_ROLE`() {
            val lduCode = uniqueLduCode()

            val response = putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, NO_ROLES)

            assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `Operation is granted when requestor has MAINTAIN_REF_DATA_ROLE`() {
            val lduCode = uniqueLduCode()

            val response = putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, MAINTAIN_REF_DATA_ROLE)

            assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        }
    }

    @Nested
    @DisplayName("DELETE ${LDU_FMB_TEMPLATE}")
    inner class DeleteFmbOnLdu {

        @Test
        fun `delete an FMB`() {
            val lduCode = uniqueLduCode()

            putLduFmb(PROBATION_AREA_CODE, lduCode, FMB1, SYSTEM_USER_ROLE)
            assertThat(deleteLduFmb(PROBATION_AREA_CODE, lduCode, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(deleteLduFmb(PROBATION_AREA_CODE, lduCode, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("PUT ${TEAM_FMB_TEMPLATE}")
    inner class PutTeamFmb {
        @Test
        fun `Create a new FMB for a team`() {
            val lduCode = uniqueLduCode()

            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(putLduTeam(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.CREATED)

            val content = jsonTester.from(getLdu(PROBATION_AREA_CODE, lduCode).body)
            assertThat(content).extractingJsonPathStringValue("$.probationTeams.${TEAM_1_CODE}.functionalMailbox").isEqualTo(FMB1)
        }

        @Test
        fun `Update an FMB for a team`() {
            val lduCode = uniqueLduCode()

            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(putLduTeam(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(putLduTeam(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB2, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)

            val content = jsonTester.from(getLdu(PROBATION_AREA_CODE, lduCode).body)
            assertThat(content).extractingJsonPathStringValue("$.probationTeams.${TEAM_1_CODE}.functionalMailbox").isEqualTo(FMB2)
        }
    }

    @Nested
    @DisplayName("DELETE ${TEAM_FMB_TEMPLATE}")
    inner class DeleteTeamFmb {

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

        @Test
        fun `Team has FMB, parent LDU has one Team, no FMB`() {
            val lduCode = uniqueLduCode()
            putLduTeam(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE)
            assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
            assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }

        @Test
        fun `Team has FMB, parent LDU has two Teams, no FMB`() {
            val lduCode = uniqueLduCode()
            putLduTeam(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE)
            putLduTeam(PROBATION_AREA_CODE, lduCode, TEAM_2_CODE, FMB2, SYSTEM_USER_ROLE)
            assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
            assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.OK)
        }

        @Test
        fun `Team has FMB, parent LDU has one team and FMB`() {
            val lduCode = uniqueLduCode()
            putLduTeam(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, FMB1, SYSTEM_USER_ROLE)
            putLduFmb(PROBATION_AREA_CODE, lduCode, FMB2, SYSTEM_USER_ROLE)
            assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NO_CONTENT)
            assertThat(deleteTeamFmb(PROBATION_AREA_CODE, lduCode, TEAM_1_CODE, SYSTEM_USER_ROLE).statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            assertThat(getLdu(PROBATION_AREA_CODE, lduCode).statusCode).isEqualTo(HttpStatus.OK)
        }
    }

    @Nested
    @DisplayName("Validation tests")
    inner class ValidationTests {

        @Nested
        @DisplayName("PUT ${LDU_FMB_TEMPLATE}")
        inner class PutLduFmb {

            @Disabled("Failing - TODO: Fix validation!")
            @Test
            fun `Invalid probation area code`() {
                val response = putLduFmb("a", uniqueLduCode(), FMB1, SYSTEM_USER_ROLE)
                assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
            }
        }
    }


    private fun getLdu(probationAreaCode: String, lduCode: String): ResponseEntity<String> =
            testRestTemplate.exchange(
                    LDU_TEMPLATE,
                    HttpMethod.GET,
                    createHttpEntityWithBearerAuthorisation(A_USER, NO_ROLES),
                    String::class.java,
                    probationAreaCode, lduCode)

    private fun putLduFmb(probationAreaCode: String, lduCode: String, functionalMailbox: String, roles: List<String>): ResponseEntity<String> =
            testRestTemplate.exchange(
                    LDU_FMB_TEMPLATE,
                    HttpMethod.PUT,
                    createHttpEntityWithBearerAuthorisation(A_USER, roles, "\"${functionalMailbox}\""),
                    String::class.java,
                    probationAreaCode, lduCode)

    private fun putLduTeam(probationAreaCode: String, lduCode: String, teamCode: String, functionalMailbox: String, roles: List<String>): ResponseEntity<String> =
            testRestTemplate.exchange(
                    TEAM_FMB_TEMPLATE,
                    HttpMethod.PUT,
                    createHttpEntityWithBearerAuthorisation(A_USER, roles, "\"${functionalMailbox}\""),
                    String::class.java,
                    probationAreaCode, lduCode, teamCode)

    private fun deleteLduFmb(probationAreaCode: String, lduCode: String, roles: List<String>): ResponseEntity<String> =
            testRestTemplate.exchange(
                    LDU_FMB_TEMPLATE,
                    HttpMethod.DELETE,
                    createHttpEntityWithBearerAuthorisation(A_USER, roles),
                    String::class.java,
                    probationAreaCode, lduCode)

    private fun deleteTeamFmb(probationAreaCode: String, lduCode: String, teamCode: String, roles: List<String>): ResponseEntity<String> =
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