package uk.gov.justice.hmpps.probationteams.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import uk.gov.justice.hmpps.probationteams.model.Email
import uk.gov.justice.hmpps.probationteams.model.LduCode
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import uk.gov.justice.hmpps.probationteams.model.ProbationAreaCode
import uk.gov.justice.hmpps.probationteams.model.ProbationTeam
import uk.gov.justice.hmpps.probationteams.model.TeamCode
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnitRepository
import java.util.Optional

@Service
@Validated
@Transactional
class LocalDeliveryUnitService(@Autowired val repository: LocalDeliveryUnitRepository) {

  fun getProbationAreaCodes(): List<String> = repository.getProbationAreaCodes()

  fun getProbationArea(probationAreaCode: String): List<LocalDeliveryUnit> =
    repository.findByProbationAreaCode(probationAreaCode)

  fun getLocalDeliveryUnit(probationAreaCode: String, localDeliveryUnitCode: String): Optional<LocalDeliveryUnit> =
    repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(probationAreaCode, localDeliveryUnitCode)

  @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
  fun setFunctionalMailbox(
    @ProbationAreaCode probationAreaCode: String,
    @LduCode localDeliveryUnitCode: String,
    @Email proposedFunctionalMailbox: String
  ): SetOutcome =

    getLocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode)
      .map { ldu -> updateLduFunctionalMailbox(ldu, proposedFunctionalMailbox) }
      .orElseGet {
        createLduFunctionalMailbox(
          probationAreaCode,
          localDeliveryUnitCode,
          proposedFunctionalMailbox
        )
      }

  @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
  fun setFunctionalMailbox(
    @ProbationAreaCode probationAreaCode: String,
    @LduCode localDeliveryUnitCode: String,
    @TeamCode teamCode: String,
    @Email proposedFunctionalMailbox: String
  ): SetOutcome =

    getLocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode)
      .map { ldu -> setTeamFunctionalMailbox(ldu, teamCode, proposedFunctionalMailbox) }
      .orElseGet {
        createLduWithTeamFunctionalMailbox(
          probationAreaCode,
          localDeliveryUnitCode,
          teamCode,
          proposedFunctionalMailbox
        )
      }

  @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
  fun deleteFunctionalMailbox(
    @ProbationAreaCode probationAreaCode: String,
    @LduCode localDeliveryUnitCode: String
  ): DeleteOutcome =

    getLocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode)
      .map { ldu -> doDeleteFmb(ldu) }
      .orElse(DeleteOutcome.NOT_FOUND)

  @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
  fun deleteFunctionalMailbox(
    @ProbationAreaCode probationAreaCode: String,
    @LduCode localDeliveryUnitCode: String,
    @TeamCode teamCode: String
  ): DeleteOutcome =

    getLocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode)
      .map { ldu -> doDeleteFmb(ldu, teamCode) }
      .orElse(DeleteOutcome.NOT_FOUND)

  private fun updateLduFunctionalMailbox(ldu: LocalDeliveryUnit, proposedFunctionalMailbox: String): SetOutcome =
    when (ldu.functionalMailbox) {
      null -> {
        ldu.functionalMailbox = proposedFunctionalMailbox
        SetOutcome.CREATED
      }
      proposedFunctionalMailbox -> SetOutcome.NO_CHANGE
      else -> {
        ldu.functionalMailbox = proposedFunctionalMailbox
        SetOutcome.UPDATED
      }
    }

  private fun createLduFunctionalMailbox(
    probationAreaCode: String,
    localDeliveryUnitCode: String,
    proposedFunctionalMailbox: String
  ): SetOutcome {
    repository.save(LocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode, proposedFunctionalMailbox))
    return SetOutcome.CREATED
  }

  private fun setTeamFunctionalMailbox(ldu: LocalDeliveryUnit, teamCode: String, proposedFunctionalMailbox: String): SetOutcome {
    val probationTeam = ldu.probationTeams[teamCode]
    return when (probationTeam) {
      null -> {
        ldu.probationTeams[teamCode] = ProbationTeam(proposedFunctionalMailbox)
        SetOutcome.CREATED
      }
      else -> {
        probationTeam.functionalMailbox = proposedFunctionalMailbox
        SetOutcome.UPDATED
      }
    }
  }

  private fun createLduWithTeamFunctionalMailbox(
    probationAreaCode: String,
    localDeliveryUnitCode: String,
    teamCode: String,
    proposedFunctionalMailbox: String
  ): SetOutcome {
    val localDeliveryUnit = LocalDeliveryUnit(
      probationAreaCode = probationAreaCode,
      localDeliveryUnitCode = localDeliveryUnitCode,
      probationTeams = mutableMapOf(teamCode to ProbationTeam(proposedFunctionalMailbox))
    )
    repository.save(localDeliveryUnit)
    return SetOutcome.CREATED
  }

  private fun doDeleteFmb(ldu: LocalDeliveryUnit): DeleteOutcome =

    when (ldu.functionalMailbox) {
      null -> DeleteOutcome.NOT_FOUND
      else -> {
        if (ldu.probationTeams.isEmpty()) {
          repository.delete(ldu)
        } else {
          ldu.functionalMailbox = null
        }
        DeleteOutcome.DELETED
      }
    }

  private fun doDeleteFmb(ldu: LocalDeliveryUnit, teamCode: String): DeleteOutcome =

    when (ldu.probationTeams[teamCode]) {
      null -> DeleteOutcome.NOT_FOUND
      else -> {
        ldu.probationTeams.remove(teamCode)
        if (ldu.probationTeams.isEmpty() && ldu.functionalMailbox == null) {
          repository.delete(ldu)
        }
        DeleteOutcome.DELETED
      }
    }
}
