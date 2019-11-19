package uk.gov.justice.hmpps.probationteams.config;

import lombok.Getter;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Configuration
@Validated
@Getter
public class ProbationTeamsProperties {
    /**
     * OAUTH2 API Rest URL endpoint ("http://localhost:9090/auth/api")
     */
    private final String oauthApiBaseUrl;

    private final String jwtPublicKey;

    private final Duration healthTimeout;

    public ProbationTeamsProperties(@Value("${oauth.api.base.url}") @URL final String oauthApiBaseUrl,
                                    @Value("${jwt.public.key}") final String jwtPublicKey,
                                    @Value("${api.health-timeout:1s}") final Duration healthTimeout) {
        this.oauthApiBaseUrl = oauthApiBaseUrl;
        this.jwtPublicKey = jwtPublicKey;
        this.healthTimeout = healthTimeout;
    }
}
