package uk.gov.justice.hmpps.probationteams.controllers;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.hmpps.probationteams.dto.ErrorResponse;
import uk.gov.justice.hmpps.probationteams.dto.LocalDeliveryUnitDto;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit;
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnitService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = {"local-delivery-units"})
@RestController
@RequestMapping(
        value = "local-delivery-units",
        produces = APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class LocalDeliveryUnitController {

    private final LocalDeliveryUnitService localDeliveryUnitService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve all Local Delivery Units",
            nickname = "Retrieve all Local Delivery Units")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Local Delivery Unit not found"),
            @ApiResponse(code = 200, message = "OK", response = LocalDeliveryUnitDto.class)})
    public Page<LocalDeliveryUnitDto> getLocalDeliveryUnits(@PageableDefault(sort = {"code"}, direction = Sort.Direction.ASC) final Pageable pageable) {

        return localDeliveryUnitService
                .getLocalDeliveryUnits(pageable)
                .map(LocalDeliveryUnitController::fromLocalDeliveryUnit);
    }

    @GetMapping(
            path = "/{localDeliveryUnitCode}",
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve a Local Delivery Unit",
            nickname = "Retrieve a Local Delivery Unit")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Local Delivery Unit not found"),
            @ApiResponse(code = 200, message = "OK", response = LocalDeliveryUnitDto.class)})
    public ResponseEntity<LocalDeliveryUnitDto> getLocalDeliveryUnit(
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode) {

        return ResponseEntity.of(
                localDeliveryUnitService
                        .getLocalDeliveryUnit(localDeliveryUnitCode)
                        .map(LocalDeliveryUnitController::fromLocalDeliveryUnit)
        );
    }

    @GetMapping(
            path = "/{localDeliveryUnitCode}/functional-mailbox",
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve a Local Delivery Unit's functional-mailbox",
            nickname = "Retrieve a Local Delivery Unit's functional-mailbox")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Functional mailbox not found"),
            @ApiResponse(code = 200, message = "OK", response = String.class)})
    public ResponseEntity<String> getFunctionalMailbox(
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode) {

        return ResponseEntity.of(
                localDeliveryUnitService
                        .getLocalDeliveryUnit(localDeliveryUnitCode)
                        .map(LocalDeliveryUnit::getFunctionalMailbox)
        );
    }

    @PutMapping(
            path = "/{localDeliveryUnitCode}/functional-mailbox",
            consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Set the Functional Mailbox for a Local Delivery Unit",
            notes = "Set the Functional Mailbox for a Local Delivery Unit")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The functional mailbox has been set"),
            @ApiResponse(code = 404, message = "No Local Delivery Unit", response = ErrorResponse.class)})
    public ResponseEntity<Void> setFunctionalMailbox(
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode,
            @RequestBody final String proposedFunctionalMailbox) {

        final var outcome = localDeliveryUnitService.setFunctionalMailbox(localDeliveryUnitCode, proposedFunctionalMailbox);
        switch (outcome) {
            case CREATED:
                return ResponseEntity.status(HttpStatus.CREATED).build();
            case UPDATED:
                return ResponseEntity.noContent().build();
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // should never happen
        }
    }

    @DeleteMapping(path = "/{localDeliveryUnitCode}")
    @ApiOperation(value = "Delete a Local Delivery Unit (and its functional mailbox)",
            notes = "Delete a Local Delivery Unit (and its functional mailbox)")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "The Local Delivery Unit has been deleted"),
            @ApiResponse(code = 404, message = "No Local Delivery Unit having the supplied code was found", response = ErrorResponse.class)})
    public ResponseEntity<Void> deleteLocalDeliveryUnit(
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "A1234AA") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode) {
        final var outcome = localDeliveryUnitService.deleteLocalDeliveryUnit(localDeliveryUnitCode);
        switch (outcome) {
            case DELETED:
                return ResponseEntity.noContent().build();
            case NOT_FOUND:
                return ResponseEntity.notFound().build();
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // should never happen
        }
    }

    private static LocalDeliveryUnitDto fromLocalDeliveryUnit(LocalDeliveryUnit ldu) {
        return LocalDeliveryUnitDto
                .builder()
                .code(ldu.getCode())
                .functionalMailbox(ldu.getFunctionalMailbox())
                .build();
    }
}
