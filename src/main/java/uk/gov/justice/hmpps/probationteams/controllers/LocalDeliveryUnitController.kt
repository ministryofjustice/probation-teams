package uk.gov.justice.hmpps.probationteams.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.probationteams.dto.LocalDeliveryUnitDto
import uk.gov.justice.hmpps.probationteams.dto.ProbationTeamDto
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitService

@Api(tags = ["local-delivery-units"])
@RestController
@RequestMapping(
  value = ["local-delivery-units"],
  produces = [APPLICATION_JSON_VALUE]
)
class LocalDeliveryUnitController(val localDeliveryUnitService: LocalDeliveryUnitService) {

  @GetMapping
  @ApiOperation(value = "Retrieve all Local Delivery Units", nickname = "Retrieve all LDUs")
  @ApiResponses(
    value = [
      ApiResponse(code = 200, message = "OK", response = LocalDeliveryUnitDto::class, responseContainer = "List")
    ]
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
        probationTeams = probationTeams.mapValues { ProbationTeamDto(it.value.functionalMailbox) }
      )
    }
  }
}
