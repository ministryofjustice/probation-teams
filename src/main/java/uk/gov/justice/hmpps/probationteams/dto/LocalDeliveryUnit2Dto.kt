package uk.gov.justice.hmpps.probationteams.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

@ApiModel(description = "Local Delivery Unit")
data class LocalDeliveryUnit2Dto @JsonCreator constructor(
        @ApiModelProperty(required = true, value = "Probation Area code", position = 1, example = "NO2")
        @JsonProperty("probationAreaCode")
        val probationAreaCode: @NotBlank String,

        @ApiModelProperty(required = true, value = "Local Delivery Unit code", position = 1, example = "NO2SUK")
        @JsonProperty("localDeliveryUnitCode")
        val localDeliveryUnitCode: @NotBlank String,

        @ApiModelProperty(required = true, value = "Functional Mailbox", position = 2, example = "a@b.com")
        @JsonProperty("functionalMailbox")
        val functionalMailbox: @Email String? = null,

        @JsonProperty("probationTeams")
        val probationTeams: Map<String, ProbationTeamDto>? = mapOf()
)