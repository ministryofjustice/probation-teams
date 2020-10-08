package uk.gov.justice.hmpps.probationteams.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.Email

@ApiModel(description = "Probation Team")
data class ProbationTeamDto @JsonCreator constructor(
    @ApiModelProperty(required = true, value = "Functional Mailbox", position = 1, example = "a@b.com")
    @JsonProperty("functionalMailbox")
    val functionalMailbox: @Email String
)
