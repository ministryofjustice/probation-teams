package uk.gov.justice.hmpps.probationteams.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class ProbationAreaResourceTest extends ResourceTest {

    private static final String lduTemplate = "/probation-areas/{probationAreaCode}/local-delivery-units/{lduCode}";

    private static final String A_USER = "API_TEST_USER";

    private static final List<String> NO_ROLES = List.of();

    private final LduCodeGenerator generator = new LduCodeGenerator();

    @Test
    public void testGetLocalDeliveryUnitNotFound() {
        final var response = getLdu("ABC", "ABC123");
        assertThatStatus(response, NOT_FOUND.value());
        assertThat(response.getBody()).isNullOrEmpty();
    }

    @Test
    public void testGetLocalDeliveryUnitWithProbationTeams() {
        final var response = getLdu("ABC", "ABC125");
        assertThatStatus(response, OK.value());
        assertThat(getBodyAsJsonContent(response)).isEqualToJson("lduDto2WithTeams.json");
    }

    @Test
    public void testGetLocalDeliveryUnit() {
        final var response = getLdu("ABC", "ABC124");
        assertThatStatus(response, OK.value());
        assertThat(getBodyAsJsonContent(response)).isEqualToJson("lduDto2.json");
    }



    private ResponseEntity<String> getLdu(String probationAreaCode, String lduCode) {
        return testRestTemplate.exchange(
                lduTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation(A_USER, NO_ROLES),
                String.class,
                probationAreaCode, lduCode);
    }
}
