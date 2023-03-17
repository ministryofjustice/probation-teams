package uk.gov.justice.hmpps.probationteams.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import uk.gov.justice.hmpps.probationteams.model.ProbationTeam
import uk.gov.justice.hmpps.probationteams.utils.uniqueLduCode
import java.util.UUID

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@WithAnonymousUser
class LocalDeliveryUnitRepositoryTest(
  @Autowired val repository: LocalDeliveryUnitRepository,
  @Autowired val jdbcTemplate: JdbcTemplate,
) {

  @Test
  fun `Persist Local Delivery Unit`() {
    val lduCode = uniqueLduCode()
    val ldu = LocalDeliveryUnit(
      probationAreaCode = "ABC",
      localDeliveryUnitCode = lduCode,
      functionalMailbox = "pqr@stu.ltd.uk",
    )

    repository.save(ldu)
    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    assertThat(repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode))
      .hasValueSatisfying { persistentLdu ->
        assertThat(persistentLdu.id).isNotNull()

        // Business key equality
        assertThat(persistentLdu).isEqualTo(ldu)
        assertThat(ldu.createUserId).isEqualTo("anonymous")
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
    assertThat(repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode))
      .hasValueSatisfying { persistentLdu ->
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

    assertThat(repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode))
      .hasValueSatisfying { persistentLdu ->
        persistentLdu.probationTeams.remove("T1")
        persistentLdu.probationTeams["T2"] = ProbationTeam("zzz@zzz.com")
      }

    TestTransaction.flagForCommit()
    TestTransaction.end()

    TestTransaction.start()

    assertThat(repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", lduCode))
      .hasValueSatisfying { persistentLdu ->
        assertThat(persistentLdu.id).isNotNull()
        assertThat(persistentLdu.probationTeams).isEqualTo(mapOf("T2" to ProbationTeam("zzz@zzz.com")))
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

  @Test
  fun findByProbationAreaCode() {
    val probationAreaCode = "XYZ"
    repository.save(lduWithProbationTeams(uniqueLduCode(), probationAreaCode))
    repository.save(lduWithProbationTeams(uniqueLduCode(), probationAreaCode))
    repository.save(lduWithProbationTeams(uniqueLduCode(), probationAreaCode))

    TestTransaction.flagForCommit()
    TestTransaction.end()
    TestTransaction.start()

    val ldus = repository.findByProbationAreaCode(probationAreaCode)
    assertThat(ldus).hasSize(3)
  }

  @Test
  fun getProbationAreaCodes() {
    val probationAreaCodes = repository.getProbationAreaCodes()
    val actualProbationAreaCodes = probationAreaCodes()
    assertThat(probationAreaCodes).isEqualTo(actualProbationAreaCodes)
  }

  private fun probationAreaCodes() =
    jdbcTemplate.queryForList(
      """
                select distinct PROBATION_AREA_CODE 
                from LOCAL_DELIVERY_UNIT2
                order by PROBATION_AREA_CODE
      """.trimIndent(),
      String::class.java,
    )

  private fun probationTeamCount(lduId: UUID?) =
    jdbcTemplate.queryForObject(
      """
                select count(*) 
                  from PROBATION_TEAM 
                 where LOCAL_DELIVERY_UNIT_ID = ?
      """.trimIndent(),
      Long::class.java,
      lduId,
    )

  private fun lduCount(lduId: UUID?) =
    jdbcTemplate.queryForObject(
      """
                select count(*) 
                  from LOCAL_DELIVERY_UNIT2 
                 where LOCAL_DELIVERY_UNIT_ID = ?
      """.trimIndent(),
      Long::class.java,
      lduId,
    )

  companion object {
    fun lduWithProbationTeams(lduCode: String, probationAreaCode: String = "ABC"): LocalDeliveryUnit =
      LocalDeliveryUnit(
        probationAreaCode,
        localDeliveryUnitCode = lduCode,
        probationTeams = mutableMapOf(
          "T1" to ProbationTeam("t1@team.com"),
          "T2" to ProbationTeam("t2@team.com"),
        ),
      )
  }
}
