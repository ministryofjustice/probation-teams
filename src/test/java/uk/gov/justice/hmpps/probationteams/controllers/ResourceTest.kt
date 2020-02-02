package uk.gov.justice.hmpps.probationteams.controllers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.hmpps.probationteams.utils.JwtAuthenticationHelper;
import uk.gov.justice.hmpps.probationteams.utils.JwtAuthenticationHelper.JwtParameters;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.core.ResolvableType.forType;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(value = "test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public abstract class ResourceTest {

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Autowired
    protected JwtAuthenticationHelper jwtAuthenticationHelper;

    HttpEntity<?> createHttpEntityWithBearerAuthorisation(final String user, final List<String> roles) {
        final var jwt = createJwt(user, roles);
        return createHttpEntity(jwt, null);
    }

    HttpEntity<?> createHttpEntityWithBearerAuthorisation(final String user, final List<String> roles, final Object body) {
        final var jwt = createJwt(user, roles);
        return createHttpEntity(jwt, body);
    }

    HttpEntity<?> createHttpEntity(final String bearerToken, final Object body) {
        final var headers = new HttpHeaders();

        headers.add(AUTHORIZATION, "Bearer " + bearerToken);
        headers.add(ACCEPT, APPLICATION_JSON_VALUE);

        if (body != null) headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);

        return new HttpEntity<>(body, headers);
    }

    <T> void assertThatStatus(final ResponseEntity<T> response, final int status) {
        assertThat(response.getStatusCodeValue()).withFailMessage("Expecting status code value <%s> to be equal to <%s> but it was not.\nBody was\n%s", response.getStatusCodeValue(), status, response.getBody()).isEqualTo(status);
    }

    <T> void assertThatJsonFileAndStatus(final ResponseEntity<String> response, final int status, final String jsonFile) {
        assertThatStatus(response, status);
        assertThat(getBodyAsJsonContent(response)).isEqualToJson(jsonFile);
    }

    protected <T> JsonContent<T> getBodyAsJsonContent(final ResponseEntity<String> response) {
        return new JsonContent<>(getClass(), forType(String.class), Objects.requireNonNull(response.getBody()));
    }

    String createJwt(final String user, final List<String> roles) {
        return jwtAuthenticationHelper.createJwt(JwtParameters.builder()
                .username(user)
                .userId(user + "_ID")
                .roles(roles)
                .scope(List.of("read", "write"))
                .expiryTime(Duration.ofDays(1))
                .build());
    }
}

