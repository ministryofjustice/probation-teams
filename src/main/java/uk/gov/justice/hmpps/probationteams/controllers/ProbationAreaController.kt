package uk.gov.justice.hmpps.probationteams.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.probationteams.dto.ErrorResponse
import uk.gov.justice.hmpps.probationteams.dto.LocalDeliveryUnitDto
import uk.gov.justice.hmpps.probationteams.dto.ProbationAreaDto
import uk.gov.justice.hmpps.probationteams.dto.ProbationTeamDto
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit
import uk.gov.justice.hmpps.probationteams.services.DeleteOutcome
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitService
import uk.gov.justice.hmpps.probationteams.services.SetOutcome

@RestController
@RequestMapping(
  value = ["probation-areas"],
  produces = [APPLICATION_JSON_VALUE]
)
class ProbationAreaController(val localDeliveryUnitService: LocalDeliveryUnitService) {

  @GetMapping(path = ["/{probationAreaCode}"])
  @Operation(
    description = "Retrieve a Probation Area",
    responses = [
      ApiResponse(
        responseCode = "200",
        content = [
          Content(mediaType = APPLICATION_JSON_VALUE, schema = Schema(implementation = ProbationAreaDto::class))
        ]
      )
    ]
  )
  fun getProbationArea(
    @Schema(description = "Probation Area code", example = "N02", required = true)
    @PathVariable("probationAreaCode")
    probationAreaCode: String

  ): ProbationAreaDto =
    ProbationAreaDto(
      probationAreaCode,
      localDeliveryUnitService
        .getProbationArea(probationAreaCode)
        .map(::fromLocalDeliveryUnit)
        .associateBy(LocalDeliveryUnitDto::localDeliveryUnitCode)
    )

  @Operation(
    description = "Retrieve a Local Delivery Unit",
    responses = [
      ApiResponse(
        responseCode = "200",
        content = [
          Content(
            mediaType = APPLICATION_JSON_VALUE,
            schema = Schema(implementation = LocalDeliveryUnitDto::class)
          )
        ]
      ),
      ApiResponse(responseCode = "404", description = "Local Delivery Unit not found")
    ]
  )

  @GetMapping(path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}"])
  fun getLocalDeliveryUnit(

    @Schema(description = "Probation Area code", required = true, example = "N02")
    @PathVariable("probationAreaCode")
    probationAreaCode: String,

    @Schema(description = "Local Delivery Unit code", required = true, example = "N02KSUK")
    @PathVariable("localDeliveryUnitCode")
    localDeliveryUnitCode: String

  ): ResponseEntity<LocalDeliveryUnitDto> = ResponseEntity.of(
    localDeliveryUnitService
      .getLocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode)
      .map(::fromLocalDeliveryUnit)
  )

  @Operation(
    description = "Set the Functional Mailbox for a Local Delivery Unit",
    responses = [
      ApiResponse(responseCode = "201", description = "The functional mailbox has been set"),
    ]
  )

  @PutMapping(
    path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/functional-mailbox"],
    consumes = [APPLICATION_JSON_VALUE]
  )
  fun setFunctionalMailbox(

    @Schema(description = "Probation Area code", required = true, example = "N02")
    @PathVariable("probationAreaCode")
    probationAreaCode: String,

    @Schema(description = "Local Delivery Unit code", required = true, example = "N02KSUK")
    @PathVariable("localDeliveryUnitCode")
    localDeliveryUnitCode: String,

    @RequestBody
    proposedFunctionalMailbox: String

  ): ResponseEntity<Void> = when (
    localDeliveryUnitService.setFunctionalMailbox(
      probationAreaCode,
      localDeliveryUnitCode,
      proposedFunctionalMailbox
    )
  ) {
    SetOutcome.CREATED -> ResponseEntity.status(HttpStatus.CREATED).build()
    SetOutcome.UPDATED,
    SetOutcome.NO_CHANGE -> ResponseEntity.noContent().build()
  }

  @Operation(
    description = "Delete a Local Delivery Unit's functional mailbox",
    responses = [
      ApiResponse(responseCode = "204", description = "The Local Delivery Unit's functional mailbox has been deleted"),
      ApiResponse(
        responseCode = "404",
        description = "Either the LDU didn't exist or it didn't have a functional mailbox",
        content = [Content(mediaType = APPLICATION_JSON_VALUE, schema = Schema(implementation = ErrorResponse::class))]
      ),
    ]
  )

  @DeleteMapping(path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/functional-mailbox"])
  fun deleteFunctionalMailbox(

    @Schema(description = "Probation Area code", required = true, example = "N02")
    @PathVariable("probationAreaCode")
    probationAreaCode: String,

    @Schema(description = "Local Delivery Unit code", required = true, example = "N02KSUK")
    @PathVariable("localDeliveryUnitCode")
    localDeliveryUnitCode: String

  ): ResponseEntity<Void> =

    when (
      localDeliveryUnitService.deleteFunctionalMailbox(
        probationAreaCode,
        localDeliveryUnitCode
      )
    ) {
      DeleteOutcome.DELETED -> ResponseEntity.noContent().build()
      DeleteOutcome.NOT_FOUND -> ResponseEntity.notFound().build()
    }

  @Operation(
    description = "Set the Functional Mailbox for a Probation team",
    responses = [
      ApiResponse(responseCode = "201", description = "The functional mailbox has been set"),
    ]
  )

  @PutMapping(
    path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/teams/{teamCode}/functional-mailbox"],
    consumes = [APPLICATION_JSON_VALUE]
  )
  fun setFunctionalMailbox(

    @Schema(description = "Probation Area code", required = true, example = "N02")
    @PathVariable("probationAreaCode")
    probationAreaCode: String,

    @Schema(description = "Local Delivery Unit code", required = true, example = "N02KSUK")
    @PathVariable("localDeliveryUnitCode")
    localDeliveryUnitCode: String,

    @Schema(description = "Team code", required = true, example = "N02KSUK")
    @PathVariable("teamCode")
    teamCode: String,

    @RequestBody
    proposedFunctionalMailbox: String

  ): ResponseEntity<Void> = when (
    localDeliveryUnitService.setFunctionalMailbox(
      probationAreaCode,
      localDeliveryUnitCode,
      teamCode,
      proposedFunctionalMailbox
    )
  ) {
    SetOutcome.CREATED -> ResponseEntity.status(HttpStatus.CREATED).build()
    SetOutcome.UPDATED,
    SetOutcome.NO_CHANGE -> ResponseEntity.noContent().build()
  }

  @Operation(
    description = "Delete a Probation Teams's functional mailbox",
    responses = [
      ApiResponse(responseCode = "204", description = "The Probation Team's functional mailbox has been deleted"),
      ApiResponse(
        responseCode = "404",
        description = "The Probation Team didn't have a functional mailbox",
        content = [Content(mediaType = APPLICATION_JSON_VALUE, schema = Schema(implementation = ErrorResponse::class))]
      ),
    ]
  )

  @DeleteMapping(path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/teams/{teamCode}/functional-mailbox"])
  fun deleteFunctionalMailbox(

    @Schema(description = "Probation Area code", required = true, example = "N02")
    @PathVariable("probationAreaCode")
    probationAreaCode: String,

    @Schema(description = "Local Delivery Unit code", required = true, example = "N02KSUK")
    @PathVariable("localDeliveryUnitCode")
    localDeliveryUnitCode: String,

    @Schema(description = "Team code", required = true, example = "N02KSUK")
    @PathVariable("teamCode")
    teamCode: String

  ): ResponseEntity<Void> = when (
    localDeliveryUnitService.deleteFunctionalMailbox(
      probationAreaCode,
      localDeliveryUnitCode,
      teamCode
    )
  ) {
    DeleteOutcome.DELETED -> ResponseEntity.noContent().build()
    DeleteOutcome.NOT_FOUND -> ResponseEntity.notFound().build()
  }

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
