package uk.gov.justice.hmpps.probationteams

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
open class ProbationTeamsApplication

fun main(args: Array<String>) {
  runApplication<ProbationTeamsApplication>(*args)
}
