package uk.gov.justice.hmpps.probationteams

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableResourceServer
@EnableTransactionManagement
open class ProbationTeamsApplication

fun main(args: Array<String>) {
  runApplication<ProbationTeamsApplication>(*args)
}
