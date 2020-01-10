package uk.gov.justice.hmpps.probationteams.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Email;

@ApiModel(description = "Probation Team")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString

public class ProbationTeamDto {
    @ApiModelProperty(required = true, value = "Functional Mailbox", position = 1, example = "a@b.com")
    @Email
    private String functionalMailbox;
}
