package uk.gov.justice.hmpps.probationteams.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://probation-teams.prison.service.justice.gov.uk/").description("Prod"),
        Server().url("https://probation-teams-preprod.prison.service.justice.gov.uk/").description("PreProd"),
        Server().url("https://probation-teams-dev.prison.service.justice.gov.uk/").description("Development"),
        Server().url("http://localhost:8080").description("Local"),
      ),
    )
    .info(
      Info().title("probation-teams API")
        .version(version)
        .description("Providing Functional Mailboxes for Probation Teams")
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
    )
}
