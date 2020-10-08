package uk.gov.justice.hmpps.probationteams.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import javax.validation.constraints.NotBlank

@ApiModel(description = "Probation Area")
data class ProbationAreaDto @JsonCreator constructor(
  @ApiModelProperty(required = true, value = "Probation Area code", position = 1, example = "NO2")
  @JsonProperty("probationAreaCode")
  val probationAreaCode: @NotBlank String,

  @ApiModelProperty(required = true, value = "Local Delivery Units by Local Delivery Unit code", position = 2)
  @JsonProperty("localDeliveryUnits")
  val localDeliveryUnits: Map<String, LocalDeliveryUnitDto> = mapOf()
)
