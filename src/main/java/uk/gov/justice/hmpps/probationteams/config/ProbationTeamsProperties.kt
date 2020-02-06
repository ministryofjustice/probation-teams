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

    private final String jwtPublicKey;

    private final Duration healthTimeout;

    public ProbationTeamsProperties(@Value("${jwt.public.key}") final String jwtPublicKey,
                                    @Value("${api.health-timeout:1s}") final Duration healthTimeout) {
        this.jwtPublicKey = jwtPublicKey;
        this.healthTimeout = healthTimeout;
    }
}
