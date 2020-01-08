package uk.gov.justice.hmpps.probationteams.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
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

        assertThat(optionalOfLDU.isPresent()).isTrue();
        final var persistentLdu = optionalOfLDU.get();
        assertThat(persistentLdu.getId()).isNotNull();

        // Business key equality
        assertThat(persistentLdu).isEqualTo(ldu);

        assertThat(ldu.getCreateUserId()).isEqualTo("anonymous");

        // Check the db...
        Long count = jdbcTemplate.queryForObject("select count(*) from LOCAL_DELIVERY_UNIT2 where LOCAL_DELIVERY_UNIT_ID = ?", Long.class, persistentLdu.getId());
        assertThat(count).isEqualTo(1);
    }
}
