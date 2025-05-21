package uk.gov.justice.hmpps.probationteams.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.probationteams.security.AuthenticationFacade
import java.util.Optional

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Service(value = "auditorAware")
class AuditorAwareImpl(private val authenticationFacade: AuthenticationFacade) : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.ofNullable(authenticationFacade.getCurrentUsername())
}
