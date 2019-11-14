package uk.gov.justice.hmpps.probationteams.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.hmpps.probationteams.utils.JwtAuthInterceptor;

import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    private final ProbationTeamsProperties properties;

    public RestTemplateConfiguration(final ProbationTeamsProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "oauthApiRestTemplate")
    public RestTemplate oauthApiRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, properties.getOauthApiBaseUrl());
    }

    @Bean(name = "oauthApiHealthRestTemplate")
    public RestTemplate oauthApiRestHealthTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getHealthRestTemplate(restTemplateBuilder, properties.getOauthApiBaseUrl());
    }


    private RestTemplate getRestTemplate(final RestTemplateBuilder restTemplateBuilder, final String uri) {
        return restTemplateBuilder
                .rootUri(uri)
                .additionalInterceptors(getRequestInterceptors())
                .build();
    }

    private RestTemplate getHealthRestTemplate(final RestTemplateBuilder restTemplateBuilder, final String uri) {
        return restTemplateBuilder
                .rootUri(uri)
                .additionalInterceptors(getRequestInterceptors())
                .setConnectTimeout(properties.getHealthTimeout())
                .setReadTimeout(properties.getHealthTimeout())
                .build();
    }

    private List<ClientHttpRequestInterceptor> getRequestInterceptors() {
        return List.of(new JwtAuthInterceptor());
    }
}
