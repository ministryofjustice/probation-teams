package uk.gov.justice.hmpps.probationteams.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2
import uk.gov.justice.hmpps.probationteams.model.ProbationTeam
import uk.gov.justice.hmpps.probationteams.utils.uniqueLduCode
import java.util.*

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@WithAnonymousUser

class LocalDeliveryUnit2RepositoryTest(
        @Autowired val repository: LocalDeliveryUnit2Repository,
        @Autowired val jdbcTemplate: JdbcTemplate
) {

    @Test
    fun `Persist Local Delivery Unit`() {
        val lduCode = uniqueLduCode()
        val ldu = LocalDeliveryUnit2(
                probationAreaCode = "ABC",
                localDeliveryUnitCode = lduCode,
                functionalMailbox = "pqr@stu.ltd.uk")

        repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()
        val optionalOfLDU = repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)

        assertThat(optionalOfLDU).isPresent
        val persistentLdu = optionalOfLDU.get()
        assertThat(persistentLdu.id).isNotNull()


        // Business key equality
        assertThat(persistentLdu).isEqualTo(ldu)

        assertThat(ldu.createUserId).isEqualTo("anonymous")

        // Check the db...
        val count = lduCount(persistentLdu.id)
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `Persist Local Delivery Unit with Probation Teams`() {
        val lduCode = uniqueLduCode()
        val ldu: LocalDeliveryUnit2 = lduWithProbationTeams(lduCode)

        repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()
        val optionalOfLdu = repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)
        assertThat(optionalOfLdu).isPresent
        val persistentLdu = optionalOfLdu.get()
        assertThat(persistentLdu.id).isNotNull()

        assertThat(persistentLdu.probationTeams).isEqualTo(lduWithProbationTeams(lduCode).probationTeams)

        val id = persistentLdu.id
        if (id != null) {
            assertThat(probationTeamCount(id)).isEqualTo(2)
        } else {
            fail("No ID for persisted LDU")
        }
    }


    @Test
    fun `Update Probation Team`() {
        val lduCode = uniqueLduCode()
        val ldu: LocalDeliveryUnit2 = lduWithProbationTeams(lduCode)
        repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()

        val persistentLduOpt = repository
                .findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)

        if (persistentLduOpt.isEmpty) {
            fail("LDU not found")
        } else {
            val persistentLdu = persistentLduOpt.get()
            persistentLdu.probationTeams.remove("T1")
            persistentLdu.probationTeams["T2"] = ProbationTeam("zzz@zzz.com")
        }

        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()

        val updatedPersistentLduOpt = repository
                .findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)

        if (updatedPersistentLduOpt.isEmpty) {
            fail("Updated LDU not found in db")
        } else {
            val updatedLdu = updatedPersistentLduOpt.get()
            assertThat(updatedLdu.probationTeams).isEqualTo(mutableMapOf("T2" to ProbationTeam("zzz@zzz.com")))

            val lduId = updatedLdu.id
            if (lduId != null) {
                assertThat(probationTeamCount(lduId)).isEqualTo(1)
            } else {
                fail("No ID for persisted LDU")
            }
        }
    }

    @Test
    fun `Deleting an LDU should delete its probation teams`() {
        val lduCode = uniqueLduCode()
        val ldu: LocalDeliveryUnit2 = lduWithProbationTeams(lduCode)

        val persistentLdu = repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        // Check the db...
        val lduId = persistentLdu.id

        if (lduId != null) {
            TestTransaction.start()
            assertThat(lduCount(lduId)).isEqualTo(1)
            assertThat(probationTeamCount(lduId)).isEqualTo(2)
            repository.deleteById(lduId)
            TestTransaction.flagForCommit()
            TestTransaction.end()

            TestTransaction.start()

            assertThat(lduCount(lduId)).isEqualTo(0)
            assertThat(probationTeamCount(lduId)).isEqualTo(0)
        } else {
            Assertions.fail("The saved LDU did not have an Id")
        }
    }

    private fun probationTeamCount(lduId: UUID) =
            jdbcTemplate.queryForObject("""
                select count(*) 
                  from PROBATION_TEAM 
                 where LOCAL_DELIVERY_UNIT_ID = ?
                 """.trimIndent(), Long::class.java, lduId)

    private fun lduCount(lduId: UUID?) =
            jdbcTemplate.queryForObject("""
                select count(*) 
                  from LOCAL_DELIVERY_UNIT2 
                 where LOCAL_DELIVERY_UNIT_ID = ?
                 """.trimIndent(), Long::class.java, lduId)

    companion object {
        fun lduWithProbationTeams(lduCode: String): LocalDeliveryUnit2 = LocalDeliveryUnit2(
                probationAreaCode = "ABC",
                localDeliveryUnitCode = lduCode,
                probationTeams = mutableMapOf(
                        "T1" to ProbationTeam("t1@team.com"),
                        "T2" to ProbationTeam("t2@team.com")
                ))
    }
}