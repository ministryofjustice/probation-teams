package uk.gov.justice.hmpps.probationteams.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LocalDeliveryUnitDto @JsonCreator constructor(
  @field:Schema(description = "Probation Area code", example = "NO2", required = true)
  @field:JsonProperty("probationAreaCode")
  val probationAreaCode: @NotBlank String,

  @field:Schema(description = "Local Delivery Unit code", example = "NO2SUK", required = true)
  @field:JsonProperty("localDeliveryUnitCode")
  val localDeliveryUnitCode: @NotBlank String,

  @field:Schema(description = "Functional Mailbox", example = "a@b.com", required = true)
  @field:JsonProperty("functionalMailbox")
  val functionalMailbox: @Email String? = null,

  @field:JsonProperty("probationTeams")
  val probationTeams: Map<String, ProbationTeamDto>? = mapOf(),
)
