package uk.gov.justice.hmpps.probationteams.health;

import org.junit.Test;
import uk.gov.justice.hmpps.probationteams.controllers.ResourceTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckTest extends ResourceTest {
    @Test
    public void testDown() {
        oauthMockServer.subPing(404);

        final var response = testRestTemplate.getForEntity("/health", String.class);
        assertThatJson(response.getBody()).node("components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException$NotFound: 404 Not Found");
        assertThatJson(response.getBody()).node("status").isEqualTo("DOWN");
        assertThat(response.getStatusCodeValue()).isEqualTo(503);
    }

    @Test
    public void testUp() {
        oauthMockServer.subPing(200);

        final var response = testRestTemplate.getForEntity("/health", String.class);
        assertThatJson(response.getBody()).node("components.OAuthApiHealth.details.HttpStatus").isEqualTo("OK");
        assertThatJson(response.getBody()).node("status").isEqualTo("UP");
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testTeapot() {
        oauthMockServer.subPing(418);

        final var response = testRestTemplate.getForEntity("/health", String.class);
        assertThatJson(response.getBody()).node("components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.client.HttpClientErrorException: 418 418");
        assertThatJson(response.getBody()).node("status").isEqualTo("DOWN");
        assertThat(response.getStatusCodeValue()).isEqualTo(503);
    }

    @Test
    public void testUpTimeout() {
        oauthMockServer.subPingDelay(200);

        final var response = testRestTemplate.getForEntity("/health", String.class);
        assertThatJson(response.getBody()).node("components.OAuthApiHealth.details.error").isEqualTo("org.springframework.web.client.ResourceAccessException: I/O error on GET request for \\\"http://localhost:8998/auth/ping\\\": Read timed out; nested exception is java.net.SocketTimeoutException: Read timed out");
        assertThatJson(response.getBody()).node("status").isEqualTo("DOWN");
        assertThat(response.getStatusCodeValue()).isEqualTo(503);
    }
}

