package uk.gov.justice.hmpps.probationteams.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitService

@Api(tags = ["probation-area-codes"])
@RestController
@RequestMapping(
    value = ["probation-area-codes"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)

class ProbationAreaCodesController(val localDeliveryUnitService: LocalDeliveryUnitService) {

    @GetMapping
    @ApiOperation(value = "Retrieve all Probation Area codes", nickname = "Retrieve all Probation Area codes")
    @ApiResponses(
        value = [
            ApiResponse(code = 200, message = "OK", response = List::class)
        ]
    )
    fun getProbationAreaCodes(): List<String> = localDeliveryUnitService.getProbationAreaCodes()
}
