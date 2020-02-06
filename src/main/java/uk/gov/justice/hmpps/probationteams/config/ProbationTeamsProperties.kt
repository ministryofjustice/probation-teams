package uk.gov.justice.hmpps.probationteams.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import java.time.Duration

@Configuration
@Validated
class ProbationTeamsProperties(
        @param:Value("\${jwt.public.key}") val jwtPublicKey: String,
        @param:Value("\${api.health-timeout:1s}") val healthTimeout: Duration
)