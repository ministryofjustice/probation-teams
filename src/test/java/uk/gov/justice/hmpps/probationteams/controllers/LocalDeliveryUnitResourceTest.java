package uk.gov.justice.hmpps.probationteams.controllers;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDeliveryUnitResourceTest extends ResourceTest {

    private static final String lduTemplate = "/local-delivery-unit/{lduCode}";
    private static final String fmbTemplate = "/local-delivery-unit/{lduCode}/functional-mail-box";
    @Test
    public void testGetLocalDeliveryUnitNotFound() {
        final var response = testRestTemplate.exchange(
                lduTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", List.of()),
                new ParameterizedTypeReference<String>() {},
                "ABC123");

        assertThatStatus(response, 404);
        assertThat(response.getBody()).isNullOrEmpty();
    }

    @Test
    public void testGetLocalDelivery() {
        final var response = testRestTemplate.exchange(
                lduTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", List.of()),
                new ParameterizedTypeReference<String>() {},
                "ABC124");

        assertThatJsonFileAndStatus(response,200, "lduDto.json");
    }

    @Test
    public void testGetFunctionalMailBox() {
        final var response = testRestTemplate.exchange(
                fmbTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", List.of()),
                new ParameterizedTypeReference<String>() {},
                "ABC125");

        assertThatStatus(response,200);
        assertThat(response.getBody()).isEqualTo("a@b.com");
    }

    @Test
    public void testGetFunctionalMailBoxNoLdu() {
        final var response = testRestTemplate.exchange(
                fmbTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", List.of()),
                new ParameterizedTypeReference<String>() {},
                "ABC999");

        assertThatStatus(response,404);
        assertThat(response.getBody()).isNullOrEmpty();
    }

    @Test
    public void testGetFunctionalMailBoxNoFmb() {
        final var response = testRestTemplate.exchange(
                fmbTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation("API_TEST_USER", List.of()),
                new ParameterizedTypeReference<String>() {},
                "ABC123");

        assertThatStatus(response,404);
        assertThat(response.getBody()).isNullOrEmpty();
    }
}
