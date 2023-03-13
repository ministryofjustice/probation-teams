package uk.gov.justice.hmpps.probationteams.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.NotBlank

data class ProbationAreaDto @JsonCreator constructor(
  @Schema(required = true, description = "Probation Area code", example = "NO2")
  @JsonProperty("probationAreaCode")
  val probationAreaCode: @NotBlank String,

  @Schema(required = true, description = "Local Delivery Units by Local Delivery Unit code")
  @JsonProperty("localDeliveryUnits")
  val localDeliveryUnits: Map<String, LocalDeliveryUnitDto> = mapOf(),
)
