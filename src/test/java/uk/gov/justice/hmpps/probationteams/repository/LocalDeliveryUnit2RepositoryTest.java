package uk.gov.justice.hmpps.probationteams.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2;
import uk.gov.justice.hmpps.probationteams.model.ProbationTeam;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
@WithAnonymousUser
public class LocalDeliveryUnit2RepositoryTest {

    @Autowired
    private LocalDeliveryUnit2Repository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testPersistLocalDeliveryUnit() {
        final var ldu = LocalDeliveryUnit2.builder().probationAreaCode("ABC").localDeliveryUnitCode("ABC123X").functionalMailbox("pqr@stu.ltd.uk").build();

        repository.save(ldu);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        final var optionalOfLDU = repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", "ABC123X");

        assertThat(optionalOfLDU).isPresent();
        final var persistentLdu = optionalOfLDU.get();
        assertThat(persistentLdu.getId()).isNotNull();

        // Business key equality
        assertThat(persistentLdu).isEqualTo(ldu);

        assertThat(ldu.getCreateUserId()).isEqualTo("anonymous");

        // Check the db...
        Long count = jdbcTemplate.queryForObject("select count(*) from LOCAL_DELIVERY_UNIT2 where LOCAL_DELIVERY_UNIT_ID = ?", Long.class, persistentLdu.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void testPersistLduWithProbationTeams() {
        final var ldu = lduWithProbationTeams();

        repository.save(ldu);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        TestTransaction.start();
        final var optionalOfLdu = repository.findByProbationAreaCodeAndLocalDeliveryUnitCode("ABC", "ABC123Y");
        assertThat(optionalOfLdu).isPresent();
        final var persistentLdu = optionalOfLdu.get();
        assertThat(persistentLdu.getId()).isNotNull();

        assertThat(persistentLdu.getProbationTeams()).isEqualTo(lduWithProbationTeams().getProbationTeams());
    }

    private LocalDeliveryUnit2 lduWithProbationTeams() {
        return LocalDeliveryUnit2
                .builder()
                .probationAreaCode("ABC")
                .localDeliveryUnitCode("ABC123Y")
                .probationTeams(
                        Map.of(
                                "T1", ProbationTeam.builder().functionalMailbox("t1@team.com").build(),
                                "T2", ProbationTeam.builder().functionalMailbox("t2@team.com").build()
                        ))
                .build();
    }
}
