package uk.gov.justice.hmpps.probationteams.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class LocalDeliveryUnitDto @JsonCreator constructor(
  @Schema(description = "Probation Area code", example = "NO2", required = true)
  @JsonProperty("probationAreaCode")
  val probationAreaCode: @NotBlank String,

  @Schema(description = "Local Delivery Unit code", example = "NO2SUK", required = true)
  @JsonProperty("localDeliveryUnitCode")
  val localDeliveryUnitCode: @NotBlank String,

  @Schema(description = "Functional Mailbox", example = "a@b.com", required = true)
  @JsonProperty("functionalMailbox")
  val functionalMailbox: @Email String? = null,

  @JsonProperty("probationTeams")
  val probationTeams: Map<String, ProbationTeamDto>? = mapOf(),
)
