package uk.gov.justice.hmpps.probationteams.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@ApiModel(description = "Local Delivery Unit")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class LocalDeliveryUnitDto {

    @ApiModelProperty(required = true, value = "Local Delivery Unit code", position = 1, example = "NO2")
    @NotBlank
    private String code;

    @ApiModelProperty(required = true, value = "Functional Mailbox", position = 2, example = "a@b.com")
    @Email
    private String functionalMailBox;
}
