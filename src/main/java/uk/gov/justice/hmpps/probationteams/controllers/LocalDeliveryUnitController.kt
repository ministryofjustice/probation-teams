package uk.gov.justice.hmpps.probationteams.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.probationteams.dto.LocalDeliveryUnitDto
import uk.gov.justice.hmpps.probationteams.dto.ProbationTeamDto
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitService

@RestController
@RequestMapping(
  value = ["local-delivery-units"],
  produces = [APPLICATION_JSON_VALUE],
)
@PreAuthorize("hasAnyRole('VIEW_PROBATION_TEAMS')")
class LocalDeliveryUnitController(val localDeliveryUnitService: LocalDeliveryUnitService) {

  @GetMapping
  @Operation(
    description = "Retrieve all Local Delivery Units",
    summary = "Retrieve all LDUs",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "JSON object of imprisonment statuses and movement reasons",
        content = [
          Content(mediaType = APPLICATION_JSON_VALUE, array = ArraySchema(schema = Schema(implementation = LocalDeliveryUnitDto::class))),
        ],
      ),
    ],
  )
  fun getAllLocalDeliveryUnits(): List<LocalDeliveryUnitDto> =
    localDeliveryUnitService
      .getLocalDeliveryUnits()
      .map(::fromLocalDeliveryUnit)

  companion object {

    private fun fromLocalDeliveryUnit(ldu: LocalDeliveryUnit) = with(ldu) {
      LocalDeliveryUnitDto(
        probationAreaCode = probationAreaCode,
        localDeliveryUnitCode = localDeliveryUnitCode,
        functionalMailbox = functionalMailbox,
        probationTeams = probationTeams.mapValues { ProbationTeamDto(it.value.functionalMailbox) },
      )
    }
  }
}
