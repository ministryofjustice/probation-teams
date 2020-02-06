package uk.gov.justice.hmpps.probationteams.repository

import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import uk.gov.justice.hmpps.probationteams.model.ProbationTeam
import uk.gov.justice.hmpps.probationteams.utils.uniqueLduCode
import java.util.*

@ActiveProfiles("test")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@WithAnonymousUser

class LocalDeliveryUnitRepositoryTest(
        @Autowired val repository: LocalDeliveryUnitRepository,
        @Autowired val jdbcTemplate: JdbcTemplate
) {

    @Test
    fun `Persist Local Delivery Unit`() {
        val lduCode = uniqueLduCode()
        val ldu = LocalDeliveryUnit(
                probationAreaCode = "ABC",
                localDeliveryUnitCode = lduCode,
                functionalMailbox = "pqr@stu.ltd.uk")

        repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()
        val optionalOfLDU = repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)

        assertThat(optionalOfLDU).isPresent

        optionalOfLDU.ifPresent { persistentLdu ->
            assertThat(persistentLdu.id).isNotNull()

            // Business key equality
            assertThat(persistentLdu).isEqualTo(ldu)

            assertThat(ldu.createUserId).isEqualTo("anonymous")

            // Check the db...
            assertThat(lduCount(persistentLdu.id)).isEqualTo(1)
        }
    }

    @Test
    fun `Persist Local Delivery Unit with Probation Teams`() {
        val lduCode = uniqueLduCode()
        val ldu: LocalDeliveryUnit = lduWithProbationTeams(lduCode)

        repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()
        val optionalOfLdu = repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)
        assertThat(optionalOfLdu).isPresent

        optionalOfLdu.ifPresent { persistentLdu ->
            assertThat(persistentLdu.id).isNotNull()
            assertThat(persistentLdu.probationTeams).isEqualTo(lduWithProbationTeams(lduCode).probationTeams)
            assertThat(probationTeamCount(persistentLdu.id)).isEqualTo(2)
        }
    }


    @Test
    fun `Update Probation Team`() {
        val lduCode = uniqueLduCode()
        val ldu: LocalDeliveryUnit = lduWithProbationTeams(lduCode)
        repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()

        val persistentLduOpt = repository
                .findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)

        assertThat(persistentLduOpt).isPresent

        persistentLduOpt.ifPresent { persistentLdu ->
            persistentLdu.probationTeams.remove("T1")
            persistentLdu.probationTeams["T2"] = ProbationTeam("zzz@zzz.com")
        }

        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()

        val updatedPersistentLduOpt = repository
                .findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode)

        assertThat(updatedPersistentLduOpt).isPresent

        updatedPersistentLduOpt.ifPresent { persistentLdu ->
            assertThat(persistentLdu.id).isNotNull()
            assertThat(persistentLdu.probationTeams).isEqualTo(mutableMapOf("T2" to ProbationTeam("zzz@zzz.com")))
            assertThat(probationTeamCount(persistentLdu.id)).isEqualTo(1)
        }
    }

    @Test
    fun `Deleting an LDU should delete its probation teams`() {
        val lduCode = uniqueLduCode()
        val ldu: LocalDeliveryUnit = lduWithProbationTeams(lduCode)

        val persistentLdu = repository.save(ldu)
        TestTransaction.flagForCommit()
        TestTransaction.end()

        // Check the db...
        val lduId = persistentLdu.id

        assertThat(lduId).isNotNull()

        TestTransaction.start()
        assertThat(lduCount(lduId)).isEqualTo(1)
        assertThat(probationTeamCount(lduId)).isEqualTo(2)
        repository.deleteById(lduId!!)

        TestTransaction.flagForCommit()
        TestTransaction.end()

        TestTransaction.start()

        assertThat(lduCount(lduId)).isEqualTo(0)
        assertThat(probationTeamCount(lduId)).isEqualTo(0)
    }

    private fun probationTeamCount(lduId: UUID?) =
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
        fun lduWithProbationTeams(lduCode: String): LocalDeliveryUnit = LocalDeliveryUnit(
                probationAreaCode = "ABC",
                localDeliveryUnitCode = lduCode,
                probationTeams = mutableMapOf(
                        "T1" to ProbationTeam("t1@team.com"),
                        "T2" to ProbationTeam("t2@team.com")
                ))
    }
}