package uk.gov.justice.hmpps.probationteams.controllers

import io.swagger.annotations.*
import  lombok.extern.slf4j.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.hmpps.probationteams.dto.ErrorResponse
import uk.gov.justice.hmpps.probationteams.dto.LocalDeliveryUnit2Dto
import uk.gov.justice.hmpps.probationteams.dto.ProbationTeamDto
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2
import uk.gov.justice.hmpps.probationteams.services.DeleteOutcome
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnit2Service
import uk.gov.justice.hmpps.probationteams.services.SetOutcome

@Api(tags = ["probation-areas"])
@RestController
@RequestMapping(
        value = ["probation-areas"],
        produces = [APPLICATION_JSON_VALUE])
@Slf4j
class ProbationAreaController(val localDeliveryUnitService: LocalDeliveryUnit2Service) {

    @GetMapping(
            path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}"],
            produces = [APPLICATION_JSON_VALUE])

    @ApiOperation(value = "Retrieve a Local Delivery Unit", nickname = "Retrieve a Local Delivery Unit")
    @ApiResponses(value = [
        ApiResponse(code = 404, message = "Local Delivery Unit not found"),
        ApiResponse(code = 200, message = "OK", response = LocalDeliveryUnit2Dto::class)
    ])
    fun getLocalDeliveryUnit(
            @ApiParam(value = "Probation Area code", required = true, example = "N02")
            @PathVariable("probationAreaCode")
            probationAreaCode: String,

            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK")
            @PathVariable("localDeliveryUnitCode")
            localDeliveryUnitCode: String
    ): ResponseEntity<LocalDeliveryUnit2Dto> = ResponseEntity.of(
            localDeliveryUnitService
                    .getLocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode)
                    .map(::fromLocalDeliveryUnit)
    )

    @PutMapping(
            path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/functional-mailbox"],
            consumes = [APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Set the Functional Mailbox for a Local Delivery Unit", notes = "Set the Functional Mailbox for a Local Delivery Unit")
    @ApiResponses(value = [
        ApiResponse(code = 201, message = "The functional mailbox has been set")
    ])
    fun setFunctionalMailbox(
            @ApiParam(value = "Probation Area code", required = true, example = "N02")
            @PathVariable("probationAreaCode")
            probationAreaCode: String,

            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK")
            @PathVariable("localDeliveryUnitCode")
            localDeliveryUnitCode: String,

            @RequestBody
            proposedFunctionalMailbox: String

    ): ResponseEntity<Void> = when (
        localDeliveryUnitService.setFunctionalMailbox(
                probationAreaCode,
                localDeliveryUnitCode,
                proposedFunctionalMailbox)) {
        SetOutcome.CREATED -> ResponseEntity.status(HttpStatus.CREATED).build()
        SetOutcome.UPDATED,
        SetOutcome.NO_CHANGE -> ResponseEntity.noContent().build()
    }

    @DeleteMapping(path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/functional-mailbox"])
    @ApiOperation(value = "Delete a Local Delivery Unit's functional mailbox", notes = "Delete a Local Delivery Unit's functional mailbox")
    @ApiResponses(value = [
        ApiResponse(code = 204, message = "The Local Delivery Unit's functional mailbox has been deleted"),
        ApiResponse(code = 404, message = "Either the LDU didn't exist or it didn't have a functional mailbox", response = ErrorResponse::class)])
    fun deleteFunctionalMailbox(
            @ApiParam(value = "Probation Area code", required = true, example = "N02")
            @PathVariable("probationAreaCode")
            probationAreaCode: String,

            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK")
            @PathVariable("localDeliveryUnitCode") localDeliveryUnitCode: String
    ): ResponseEntity<Void> =

            when (
                localDeliveryUnitService.deleteFunctionalMailbox(
                        probationAreaCode,
                        localDeliveryUnitCode)) {
                DeleteOutcome.DELETED -> ResponseEntity.noContent().build()
                DeleteOutcome.NOT_FOUND -> ResponseEntity.notFound().build()
            }

    @PutMapping(
            path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/teams/{teamCode}/functional-mailbox"],
            consumes = [APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Set the Functional Mailbox for a Probation team", notes = "Set the Functional Mailbox for a Probation team")
    @ApiResponses(value = [
        ApiResponse(code = 201, message = "The functional mailbox has been set")
    ])
    fun setFunctionalMailbox(
            @ApiParam(value = "Probation Area code", required = true, example = "N02")
            @PathVariable("probationAreaCode")
            probationAreaCode: String,

            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK")
            @PathVariable("localDeliveryUnitCode")
            localDeliveryUnitCode: String,

            @ApiParam(value = "Team code", required = true, example = "N02KSUK")
            @PathVariable("teamCode")
            teamCode: String,

            @RequestBody
            proposedFunctionalMailbox: String

    ): ResponseEntity<Void> = when (
        localDeliveryUnitService.setFunctionalMailbox(
                probationAreaCode,
                localDeliveryUnitCode,
                teamCode,
                proposedFunctionalMailbox)) {
        SetOutcome.CREATED -> ResponseEntity.status(HttpStatus.CREATED).build()
        SetOutcome.UPDATED,
        SetOutcome.NO_CHANGE -> ResponseEntity.noContent().build()
    }

    @DeleteMapping(path = ["/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/teams/{teamCode}/functional-mailbox"])
    @ApiOperation(value = "Delete a Probation Teams's functional mailbox", notes = "Delete a Probation Teams's functional mailbox")
    @ApiResponses(value = [
        ApiResponse(code = 204, message = "The Probation Team's functional mailbox has been deleted"),
        ApiResponse(code = 404, message = "The Probation Team didn't have a functional mailbox", response = ErrorResponse::class)])
    fun deleteFunctionalMailbox(
            @ApiParam(value = "Probation Area code", required = true, example = "N02")
            @PathVariable("probationAreaCode")
            probationAreaCode: String,

            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK")
            @PathVariable("localDeliveryUnitCode")
            localDeliveryUnitCode: String,

            @ApiParam(value = "Team code", required = true, example = "N02KSUK")
            @PathVariable("teamCode")
            teamCode: String

    ): ResponseEntity<Void> = when (
        localDeliveryUnitService.deleteFunctionalMailbox(
                probationAreaCode,
                localDeliveryUnitCode,
                teamCode)) {
        DeleteOutcome.DELETED -> ResponseEntity.noContent().build()
        DeleteOutcome.NOT_FOUND -> ResponseEntity.notFound().build()
    }

    companion object {
        private fun fromLocalDeliveryUnit(ldu: LocalDeliveryUnit2) = with(ldu) {
            LocalDeliveryUnit2Dto(
                    probationAreaCode = probationAreaCode,
                    localDeliveryUnitCode = localDeliveryUnitCode,
                    functionalMailbox = functionalMailbox,
                    probationTeams = probationTeams.mapValues { ProbationTeamDto(it.value.functionalMailbox) }
            )
        }
    }
}
