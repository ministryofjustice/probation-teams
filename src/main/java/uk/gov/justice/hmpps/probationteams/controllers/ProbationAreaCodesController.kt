package uk.gov.justice.hmpps.probationteams.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitService

@RestController
@RequestMapping(
  value = ["probation-area-codes"],
  produces = [MediaType.APPLICATION_JSON_VALUE],
)
@PreAuthorize("hasAnyRole('VIEW_PROBATION_TEAMS')")
class ProbationAreaCodesController(val localDeliveryUnitService: LocalDeliveryUnitService) {

  @GetMapping
  @Operation(
    description = "Retrieve all Probation Area codes",
    responses = [
      ApiResponse(
        responseCode = "200",
        content = [
          Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = ArraySchema(schema = Schema(implementation = String::class)),
          ),
        ],
      ),
    ],
  )
  fun getProbationAreaCodes(): List<String> = localDeliveryUnitService.getProbationAreaCodes()
}
