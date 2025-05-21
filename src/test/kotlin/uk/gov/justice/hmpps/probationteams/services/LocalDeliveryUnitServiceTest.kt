package uk.gov.justice.hmpps.probationteams.services

import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import uk.gov.justice.hmpps.probationteams.model.ProbationTeam
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnitRepository
import java.util.Optional

@ExtendWith(MockKExtension::class)
@DisplayName("LDU Service tests")
class LocalDeliveryUnitServiceTest {

  val repository: LocalDeliveryUnitRepository = mockk(relaxUnitFun = true)

  val service = LocalDeliveryUnitService(repository)

  @BeforeEach
  fun resetAllMocks() {
    clearMocks(repository)
  }

  @Nested
  @DisplayName("getProbationArea()")
  inner class GetProbationArea {

    @Test
    fun `Probation area exists`() {
      every { repository.findByProbationAreaCode(PROBATION_AREA_CODE) } returns listOf(lduWithFmb())
      assertThat(service.getProbationArea(PROBATION_AREA_CODE)).isNotEmpty
    }

    @Test
    fun `Probation area does not exist`() {
      every { repository.findByProbationAreaCode(PROBATION_AREA_CODE) } returns listOf()
      assertThat(service.getProbationArea(PROBATION_AREA_CODE)).isEmpty()
    }
  }

  @Nested
  @DisplayName("getLocalDeliveryUnit()")
  inner class GetLdu {

    @Test
    fun `LDU exists`() {
      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) } returns Optional.of(lduWithFmb())
      assertThat(service.getLocalDeliveryUnit(PROBATION_AREA_CODE, LDU_CODE)).isPresent
    }

    @Test
    fun `LDU does not exist`() {
      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) } returns Optional.empty()
      assertThat(service.getLocalDeliveryUnit(PROBATION_AREA_CODE, LDU_CODE)).isEmpty
    }
  }

  @Nested
  @DisplayName("setFunctionalMailbox() (LDU)")
  inner class SetLduFunctionalMailbox {

    @Test
    fun `No LDU found - save new LDU with FMB`() {
      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.empty()
      every { repository.save<LocalDeliveryUnit>(any()) } returns lduWithFmb()

      assertThat(service.setFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, FMB)).isEqualTo(SetOutcome.CREATED)

      verify { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) }
      verify { repository.save(lduWithFmb()) }
      confirmVerified(repository)
    }

    @Test
    fun `LDU found - update FMB`() {
      val persistentLdu = lduWithFmb()

      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(persistentLdu)

      assertThat(service.setFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, FMB2)).isEqualTo(SetOutcome.UPDATED)
      assertThat(persistentLdu.functionalMailbox).isEqualTo(FMB2)

      verify { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) }
      confirmVerified(repository)
    }

    @Test
    fun `LDU found - same FMB`() {
      val persistentLdu = lduWithFmb()
      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(persistentLdu)

      assertThat(service.setFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, FMB)).isEqualTo(SetOutcome.NO_CHANGE)
      assertThat(persistentLdu.functionalMailbox).isEqualTo(FMB)

      verify { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) }
      confirmVerified(repository)
    }

    @Nested
    @DisplayName("deleteFunctionalMailbox() (LDU)")
    inner class DeleteLduFmb {

      @Test
      fun `No LDU found`() {
        every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.empty()

        assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE)).isEqualTo(DeleteOutcome.NOT_FOUND)
      }

      @Test
      fun `Found LDU with no Teams`() {
        val persistentLdu = lduWithFmb()
        every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(persistentLdu)

        assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE)).isEqualTo(DeleteOutcome.DELETED)

        verify { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) }
        verify { repository.delete(persistentLdu) }
      }

      @Test
      fun `Found LDU with teams, but no LDU fmb`() {
        val persistentLdu = lduWithNoFmb()
        persistentLdu.probationTeams["T1"] = ProbationTeam(FMB)

        every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(persistentLdu)

        assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE)).isEqualTo(DeleteOutcome.NOT_FOUND)

        verify { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) }
      }

      @Test
      fun `Found LDU with teams, and LDU fmb`() {
        val persistentLdu = lduWithFmb()
        persistentLdu.probationTeams[TEAM_CODE_1] = ProbationTeam(FMB)
        assertThat(persistentLdu.functionalMailbox).isEqualTo(FMB)

        every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(persistentLdu)

        assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE)).isEqualTo(DeleteOutcome.DELETED)
        assertThat(persistentLdu.functionalMailbox).isNull()

        verify { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE) }
      }
    }
  }

  @Nested
  @DisplayName("setFunctionalMailbox() (Team)")
  inner class SetTeamFunctionalMailbox {

    @Test
    fun `No LDU`() {
      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.empty()
      every { repository.save<LocalDeliveryUnit>(any()) } returns lduWithFmb() // Don't care what it returns

      assertThat(service.setFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1, FMB)).isEqualTo(SetOutcome.CREATED)

      val ldu = lduWithNoFmb()
      ldu.probationTeams[TEAM_CODE_1] = ProbationTeam(FMB)

      verify { repository.save(ldu) }
    }

    @Test
    fun `LDU but no Team`() {
      val ldu = lduWithNoFmb()
      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(ldu)

      assertThat(service.setFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1, FMB)).isEqualTo(SetOutcome.CREATED)

      assertThat(ldu.probationTeams[TEAM_CODE_1]).isEqualTo(ProbationTeam(FMB))
    }

    @Test
    fun `LDU and Team`() {
      val ldu = lduWithNoFmb()
      ldu.probationTeams[TEAM_CODE_1] = ProbationTeam(FMB)
      ldu.probationTeams[TEAM_CODE_2] = ProbationTeam("Dummy")

      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(ldu)

      assertThat(service.setFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1, FMB2)).isEqualTo(SetOutcome.UPDATED)

      assertThat(ldu.probationTeams[TEAM_CODE_1]).isEqualTo(ProbationTeam(FMB2))
    }
  }

  @Nested
  @DisplayName("deleteFunctionalMailbox() (Team)")
  inner class DeleteTeamFunctionalMailbox {

    @Test
    fun `No LDU`() {
      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.empty()

      assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1)).isEqualTo(DeleteOutcome.NOT_FOUND)

      verify(inverse = true) { repository.delete(any()) }
    }

    @Test
    fun `No matching team`() {
      val ldu = lduWithFmb()
      ldu.probationTeams[TEAM_CODE_2] = ProbationTeam(FMB2)

      val preLdu = ldu.copy()
      assertThat(ldu).isEqualTo(preLdu)

      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(ldu)

      assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1)).isEqualTo(DeleteOutcome.NOT_FOUND)
      assertThat(ldu).isEqualTo(preLdu)

      verify(inverse = true) { repository.delete(any()) }
    }

    @Test
    fun `Matching team, LDU has more than one Team`() {
      val ldu = lduWithFmb()
      ldu.probationTeams[TEAM_CODE_1] = ProbationTeam(FMB)
      ldu.probationTeams[TEAM_CODE_2] = ProbationTeam(FMB2)

      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(ldu)

      assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1)).isEqualTo(DeleteOutcome.DELETED)

      val expectedLdu = lduWithFmb()
      expectedLdu.probationTeams[TEAM_CODE_2] = ProbationTeam(FMB2)

      assertThat(ldu).isEqualTo(expectedLdu)

      verify(inverse = true) { repository.delete(any()) }
    }

    @Test
    fun `Matching team, LDU has one Team, amd FMB`() {
      val ldu = lduWithFmb()
      ldu.probationTeams[TEAM_CODE_1] = ProbationTeam(FMB)

      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(ldu)

      assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1)).isEqualTo(DeleteOutcome.DELETED)
      assertThat(ldu).isEqualTo(lduWithFmb())

      verify(inverse = true) { repository.delete(any()) }
    }

    @Test
    fun `Matching team, LDU has one Team, no FMB`() {
      val ldu = lduWithNoFmb()
      ldu.probationTeams[TEAM_CODE_1] = ProbationTeam(FMB)

      every { repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(any(), any()) } returns Optional.of(ldu)

      assertThat(service.deleteFunctionalMailbox(PROBATION_AREA_CODE, LDU_CODE, TEAM_CODE_1)).isEqualTo(DeleteOutcome.DELETED)

      verify { repository.delete(ldu) }
    }
  }

  companion object {
    private const val PROBATION_AREA_CODE = "ABC"
    private const val LDU_CODE = "ABC123"
    private const val TEAM_CODE_1 = "T1"
    private const val TEAM_CODE_2 = "T2"
    private const val FMB = "a@b.com"
    private const val FMB2 = "x@y.gov.uk"

    fun lduWithFmb() = LocalDeliveryUnit(
      probationAreaCode = PROBATION_AREA_CODE,
      localDeliveryUnitCode = LDU_CODE,
      functionalMailbox = FMB,
    )

    fun lduWithNoFmb() = LocalDeliveryUnit(
      probationAreaCode = PROBATION_AREA_CODE,
      localDeliveryUnitCode = LDU_CODE,
    )
  }
}
