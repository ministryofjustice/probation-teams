package uk.gov.justice.hmpps.probationteams.controllers;

import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;

public class LocalDeliveryUnitResourceTest extends ResourceTest {

    private static final String ldusTemplate = "/local-delivery-units";
    private static final String lduTemplate = "/local-delivery-units/{lduCode}";
    private static final String fmbTemplate = "/local-delivery-units/{lduCode}/functional-mailbox";

    private static final String A_USER = "API_TEST_USER";

    private static final List<String> NO_ROLES = List.of();
    private static final List<String> SYSTEM_USER_ROLE = List.of("ROLE_SYSTEM_USER");
    private static final List<String> REF_DATA_ROLE = List.of("ROLE_MAINTAIN_REF_DATA");

    private final LduCodeGenerator generator = new LduCodeGenerator();

    @Test
    public void testGetLocalDeliveryUnitNotFound() {
        final var response = getLdu("ABC123");
        assertThatStatus(response, NOT_FOUND.value());
        assertThat(response.getBody()).isNullOrEmpty();
    }

    @Test
    public void testGetLocalDeliveryUnit() {
        final var response = getLdu("ABC125");
        assertThatJsonFileAndStatus(response, OK.value(), "lduDto.json");
    }


    @Test
    public void testGetLduNoLdu() {
        final var response = getLdu("ABC999");
        assertThatStatus(response, NOT_FOUND.value());
        assertThat(response.getBody()).isNullOrEmpty();
    }

    @Test
    public void testPutFunctionalMailbox_notAuthorised() {
        final var response = putFmb(nextLduCode(), "a@b.com", NO_ROLES);
        assertThatStatus(response, FORBIDDEN.value());
    }

    @Test
    public void testPutFunctionalMailbox_authorized_systemRole() {
        final var lduCode = nextLduCode();

        assertThatStatus(getLdu(lduCode), NOT_FOUND.value());
        assertThatStatus(putFmb(lduCode, "a@b.com", SYSTEM_USER_ROLE), CREATED.value());

        final var result = getLdu(lduCode);
        assertThatStatus(result, OK.value());
        assertThat(getBodyAsJsonContent(result)).isEqualTo(lduAsJsonString(lduCode, "a@b.com"));
    }

    @Test
    public void testPutFunctionalMailbox_authorized_refDataRole() {
        final var lduCode = nextLduCode();

        assertThatStatus(getLdu(lduCode), NOT_FOUND.value());
        assertThatStatus(putFmb(lduCode, "a@b.com", REF_DATA_ROLE), CREATED.value());

        final var result = getLdu(lduCode);
        assertThatStatus(result, OK.value());
        assertThat(getBodyAsJsonContent(result)).isEqualTo(lduAsJsonString(lduCode, "a@b.com"));
    }

    @Test
    public void testPutFunctionalMailbox_update() {
        final var lduCode = nextLduCode();

        assertThatStatus(getLdu(lduCode), NOT_FOUND.value());
        assertThatStatus(putFmb(lduCode, "a@b.com", SYSTEM_USER_ROLE), CREATED.value());

        final var saveResult = getLdu(lduCode);
        assertThatStatus(saveResult, OK.value());
        assertThat(getBodyAsJsonContent(saveResult)).isEqualTo(lduAsJsonString(lduCode, "a@b.com"));

        assertThatStatus(putFmb(lduCode, "p@q.org", SYSTEM_USER_ROLE), NO_CONTENT.value());

        final var updateResult = getLdu(lduCode);
        assertThatStatus(updateResult, OK.value());
        assertThat(getBodyAsJsonContent(updateResult)).isEqualTo(lduAsJsonString(lduCode, "p@q.org"));

    }

    @Test
    public void testPutFunctionalMailbox_badLduCode() {
        final var response = putFmb("abc-", "abc@def.com", SYSTEM_USER_ROLE);
        assertThatStatus(response, BAD_REQUEST.value());
        assertThat(getBodyAsJsonContent(response)).isEqualToJson("{\"status\":400,\"developerMessage\":\"setFunctionalMailbox.localDeliveryUnitCode: Invalid Local Delivery Unit code\"}");
    }

    @Test
    public void testPutFunctionalMailbox_badMailbox() {
        final var response = putFmb(nextLduCode(), "a@@b.com", SYSTEM_USER_ROLE);
        assertThatStatus(response, BAD_REQUEST.value());
        assertThat(getBodyAsJsonContent(response)).isEqualToJson("{\"status\":400,\"developerMessage\":\"setFunctionalMailbox.proposedFunctionalMailbox: must be a well-formed email address\"}");
    }

    @Test
    public void testDeleteFunctionalMailbox_authorized() {
        final var lduCode = nextLduCode();

        assertThatStatus(getLdu(lduCode), NOT_FOUND.value());
        assertThatStatus(putFmb(lduCode, "a@b.com", REF_DATA_ROLE), CREATED.value());
        assertThatStatus(getLdu(lduCode), OK.value());
        assertThatStatus(deleteLdu(lduCode, REF_DATA_ROLE), NO_CONTENT.value());
        assertThatStatus(getLdu(lduCode), NOT_FOUND.value());
        assertThatStatus(deleteLdu(lduCode, REF_DATA_ROLE), NO_CONTENT.value());
    }

    @Test
    public void testGetLocalDeliveryUnits() {
        final var response = getLdus();
        assertThatStatus(response, OK.value());
        assertThat(getBodyAsJsonContent(response))
                .hasJsonPathArrayValue("$.content")
                .hasJsonPathStringValue("$.content[0].code")
                .hasJsonPathStringValue("$.content[0].functionalMailbox")
                .hasJsonPathNumberValue("$.size")
                .hasJsonPathNumberValue("$.totalElements")
                .hasJsonPathNumberValue("$.totalPages")
                .hasJsonPathNumberValue("$.number");
    }

    @Test
    public void testGetLocalDeliveryUnitsWithQueryParams() {
        final var response = getLdus(Map.of(
                "page", "10",
                "size", "20",
                "sort", "functionalMailbox,desc"));

        assertThatStatus(response, OK.value());
        assertThat(getBodyAsJsonContent(response)).isEqualToJson("pagedLduDto.json");
    }

    private ResponseEntity<Void> deleteLdu(String lduCode, List<String> roles) {
        return testRestTemplate.exchange(
                lduTemplate,
                HttpMethod.DELETE,
                createHttpEntityWithBearerAuthorisation(A_USER, roles),
                Void.class,
                lduCode
        );
    }

    private ResponseEntity<String> putFmb(String lduCode, String fmb, List<String> roles) {
        return testRestTemplate.exchange(
                fmbTemplate,
                HttpMethod.PUT,
                createHttpEntityWithBearerAuthorisation(A_USER, roles, "\"" + fmb + "\""),
                String.class,
                lduCode
        );
    }

    private ResponseEntity<String> getLdu(String lduCode) {
        return testRestTemplate.exchange(
                lduTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation(A_USER, NO_ROLES),
                String.class,
                lduCode);
    }

    private ResponseEntity<String> getLdus() {
        return testRestTemplate.exchange(
                ldusTemplate,
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation(A_USER, NO_ROLES),
                String.class
        );
    }

    private ResponseEntity<String> getLdus(Map<String, String> queryParameters) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(ldusTemplate);
        for (Map.Entry<String, String> parameter : queryParameters.entrySet()) {
            builder.queryParam(parameter.getKey(), parameter.getValue());
        }
        return testRestTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                createHttpEntityWithBearerAuthorisation(A_USER, NO_ROLES),
                String.class
        );
    }


    private String nextLduCode() {
        return generator.lduCode();
    }

    private static String lduAsJsonString(String code, String functionalMailbox) {
        return String.format("{\"code\":\"%1$s\", \"functionalMailbox\": \"%2$s\"}", code, functionalMailbox);
    }
}
