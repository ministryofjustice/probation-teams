package uk.gov.justice.hmpps.probationteams.controllers;

import com.microsoft.applicationinsights.TelemetryClient;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.hmpps.probationteams.config.SecurityUserContext;
import uk.gov.justice.hmpps.probationteams.dto.ErrorResponse;
import uk.gov.justice.hmpps.probationteams.dto.LocalDeliveryUnitDto;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit;
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitService;

import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = {"local-delivery-unit"})
@RestController
@RequestMapping(
        value = "local-delivery-unit",
        produces = APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class LocalDeliveryUnitController {

    private final LocalDeliveryUnitService localDeliveryUnitService;
    private final TelemetryClient telemetryClient;
    private final SecurityUserContext securityUserContext;

    @GetMapping("/{localDeliveryUnitCode}")
    @ResponseBody
    @ApiOperation(value = "Retrieve a Local Delivery Unit",
            nickname = "Retrieve a Local Delivery Unit")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Local Delivery Unit not found"),
            @ApiResponse(code = 200, message = "OK", response = LocalDeliveryUnit.class)})
    public ResponseEntity<LocalDeliveryUnitDto> getProbationTeam(
            @ApiParam(value = "Local Delvery Unit code", required = true, example = "NO2") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode) {

        return ResponseEntity.of(
                localDeliveryUnitService
                        .getLocalDeliveryUnit(localDeliveryUnitCode)
                        .map(LocalDeliveryUnitController::fromLocalDeliveryUnit)
        );
    }

    @GetMapping("/{localDeliveryUnitCode}/functional-mail-box")
    @ResponseBody
    @ApiOperation(value = "Retrieve a Local Delivery Unit's functional mailbox",
            nickname = "Retrieve a Local Delivery Unit's functional mailbox")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Local Delivery Unit Mailbox not found"),
            @ApiResponse(code = 200, message = "OK", response = String.class)})
    public ResponseEntity<String> getProbationTeamFunctionalMailBox(
            @ApiParam(value = "Probation Team Name", required = true, example = "A1234AA") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode) {
        return ResponseEntity.of(localDeliveryUnitService.getFunctionalMailBox(localDeliveryUnitCode));
    }

    @PutMapping(value = "/{localDeliveryUnitCode}/functional-mail-box", consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set the Functional Mailbox for a Local Delivery Unit",
            notes = "Set the Functional Mailbox for a Local Delivery Unit")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The functional mailbox has been set"),
            @ApiResponse(code = 404, message = "No Local Delivery Unit", response = ErrorResponse.class)})
    public void setFunctionalMailBox(
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "A1234AA") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode,
            @RequestBody @NotNull final String proposedFunctionalMailBox) {
        localDeliveryUnitService.setFunctionalMailBox(localDeliveryUnitCode, proposedFunctionalMailBox);

    }

    @DeleteMapping(value = "/{localDeliveryUnitCode}/functional-mail-box")
    @ApiOperation(value = "Delete the Functional Mailbox for a Local Delivery Unit",
            notes = "Delete the Functional Mailbox for a Local Delivery Unit")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The functional mailbox has been deleted"),
            @ApiResponse(code = 404, message = "No Local Delivery Unit having the supplied name was found", response = ErrorResponse.class)})
    public void setFunctionalMailBox(
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "A1234AA") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode) {
        localDeliveryUnitService.deleteFunctionalMailBox(localDeliveryUnitCode);
    }

    private static LocalDeliveryUnitDto fromLocalDeliveryUnit(LocalDeliveryUnit ldu) {
        return LocalDeliveryUnitDto
                .builder()
                .code(ldu.getCode())
                .functionalMailBox(ldu.getFunctionalMailBox())
                .build();
    }
}
