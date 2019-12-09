package uk.gov.justice.hmpps.probationteams.controllers;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthResourceTest extends ResourceTest {

    private static final String PING_URL = "/health/ping";
    private static final String HEALTH_URL = "/health";
    private static final String INFO_URL = "/info";

    @Test
    public void pingTest() {
        final var response =  testRestTemplate.getForEntity(PING_URL, String.class);
        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(response)).hasJsonPathStringValue("$.status", "UP");
    }

    @Test
    public void healthTest() {
        final var response =  testRestTemplate.getForEntity(HEALTH_URL, String.class);
        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(response)).hasJsonPathStringValue("$.status", "UP");
        assertThat(getBodyAsJsonContent(response)).hasJsonPathStringValue("$.components.ping.status", "UP");
        assertThat(getBodyAsJsonContent(response)).hasJsonPathStringValue("$.components.db.status", "UP");
        assertThat(getBodyAsJsonContent(response)).hasJsonPathStringValue("$.components.diskSpace.status", "UP");
    }

    @Test
    public void infoTest() {
        final var response =  testRestTemplate.getForEntity(INFO_URL, String.class);
        assertThatStatus(response, HttpStatus.OK.value());
        assertThat(getBodyAsJsonContent(response)).hasJsonPathStringValue("$.test-message", "Info Test");
    }
}
