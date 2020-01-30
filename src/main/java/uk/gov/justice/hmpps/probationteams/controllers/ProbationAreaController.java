package uk.gov.justice.hmpps.probationteams.controllers;

import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.hmpps.probationteams.dto.ErrorResponse;
import uk.gov.justice.hmpps.probationteams.dto.LocalDeliveryUnit2Dto;
import uk.gov.justice.hmpps.probationteams.dto.ProbationTeamDto;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2;
import uk.gov.justice.hmpps.probationteams.model.ProbationTeam;
import uk.gov.justice.hmpps.probationteams.services.LocalDeliveryUnit2Service;

import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(tags = {"probation-areas"})
@RestController
@RequestMapping(
        value = "probation-areas",
        produces = APPLICATION_JSON_VALUE)
@Slf4j
@AllArgsConstructor
public class ProbationAreaController {

    private final LocalDeliveryUnit2Service localDeliveryUnitService;

    @GetMapping(
            path = "/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}",
            produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve a Local Delivery Unit",
            nickname = "Retrieve a Local Delivery Unit")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Local Delivery Unit not found"),
            @ApiResponse(code = 200, message = "OK", response = LocalDeliveryUnit2Dto.class)})
    public ResponseEntity<LocalDeliveryUnit2Dto> getLocalDeliveryUnit(
            @ApiParam(value = "Probation Area code", required = true, example = "N02") @PathVariable("probationAreaCode") final String probationAreaCode,
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode
    ) {
        return ResponseEntity.of(
                localDeliveryUnitService
                        .getLocalDeliveryUnit(probationAreaCode, localDeliveryUnitCode)
                        .map(ProbationAreaController::fromLocalDeliveryUnit)
        );
    }

    @PutMapping(
            path = "/{probationAreaCode}/local-delivery-units/{localDeliveryUnitCode}/functional-mailbox",
            consumes = APPLICATION_JSON_VALUE
    )
    @ApiOperation(value = "Set the Functional Mailbox for a Local Delivery Unit",
            notes = "Set the Functional Mailbox for a Local Delivery Unit")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The functional mailbox has been set"),
            @ApiResponse(code = 404, message = "No Probation Area or Local Delivery Unit", response = ErrorResponse.class)})
    public ResponseEntity<Void> setLduFunctionalMailbox(
            @ApiParam(value = "Probation Area code", required = true, example = "N02") @PathVariable("probationAreaCode") final String probationAreaCode,
            @ApiParam(value = "Local Delivery Unit code", required = true, example = "N02KSUK") @PathVariable("localDeliveryUnitCode") final String localDeliveryUnitCode,
            @RequestBody final String proposedFunctionalMailbox
    ) {
        final var outcome = localDeliveryUnitService.setFunctionalMailgox(probationAreaCode, localDeliveryUnitCode, proposedFunctionalMailbox);
        switch (outcome) {
            case CREATED:
                return ResponseEntity.status(HttpStatus.CREATED).build();
            case UPDATED:
                return ResponseEntity.noContent().build();
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // should never happen
        }
    }


    private static LocalDeliveryUnit2Dto fromLocalDeliveryUnit(LocalDeliveryUnit2 ldu) {
        return LocalDeliveryUnit2Dto
                .builder()
                .probationAreaCode(ldu.getProbationAreaCode())
                .localDeliveryUnitCode(ldu.getLocalDeliveryUnitCode())
                .functionalMailbox(ldu.getFunctionalMailbox())
                .probationTeams(fromProbationTeams(ldu.getProbationTeams()))
                .build();
    }

    private static Map<String, ProbationTeamDto> fromProbationTeams(Map<String, ProbationTeam> probationTeams) {
        return probationTeams
                .entrySet()
                .stream()
                .map(entry -> Map.entry(entry.getKey(), fromProbationTeam(entry.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static ProbationTeamDto fromProbationTeam(ProbationTeam probationTeam) {
        return ProbationTeamDto.builder().functionalMailbox(probationTeam.getFunctionalMailbox()).build();
    }
}
