package uk.gov.justice.hmpps.probationteams.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email

data class ProbationTeamDto @JsonCreator constructor(
  @Schema(required = true, description = "Functional Mailbox", example = "a@b.com")
  @JsonProperty("functionalMailbox")
  val functionalMailbox: @Email String
)
