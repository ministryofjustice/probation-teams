package uk.gov.justice.hmpps.probationteams.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Map;


@ApiModel(description = "Local Delivery Unit")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class LocalDeliveryUnit2Dto {

    @ApiModelProperty(required = true, value = "Probation Area code", position = 1, example = "NO2")
    @NotBlank
    private String probationAreaCode;

    @ApiModelProperty(required = true, value = "Local Delivery Unit code", position = 1, example = "NO2SUK")
    @NotBlank
    private String localDeliveryUnitCode;

    @ApiModelProperty(required = true, value = "Functional Mailbox", position = 2, example = "a@b.com")
    @Email
    private String functionalMailbox;

    private Map<String, ProbationTeamDto> probationTeams;
}
