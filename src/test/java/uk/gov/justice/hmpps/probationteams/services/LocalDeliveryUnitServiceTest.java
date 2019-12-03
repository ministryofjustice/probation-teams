package uk.gov.justice.hmpps.probationteams.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit;
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnitRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalDeliveryUnitServiceTest {
    private static final String LDU_CODE = "ABC123";
    private static final String FMB = "a@b.com";
    private static final String FMB2 = "x@y.gov.uk";

    @Mock
    private LocalDeliveryUnitRepository repository;

    private LocalDeliveryUnitService service;

    @Before
    public void setUp() {
        service = new LocalDeliveryUnitService(repository);
    }

    @Test
    public void getLocalDeliveryUnit() {

        when(repository.findByCode(LDU_CODE)).thenReturn(Optional.of(lduWithFmb()));
        assertThat(service.getLocalDeliveryUnit(LDU_CODE)).isPresent();
    }

    @Test
    public void getLocalDeliveryUnitNoLdu() {
        when(repository.findByCode(LDU_CODE)).thenReturn(Optional.empty());
        assertThat(service.getLocalDeliveryUnit(LDU_CODE)).isEmpty();
    }

    @Test
    public void getFunctionalMailbox() {
        when(repository.findByCode(LDU_CODE)).thenReturn(Optional.of(lduWithFmb()));
        assertThat(service.getFunctionalMailbox(LDU_CODE)).contains(FMB);
    }

    @Test
    public void getFunctionalMailboxNoLdu() {
        when(repository.findByCode(LDU_CODE)).thenReturn(Optional.empty());
        assertThat(service.getFunctionalMailbox(LDU_CODE)).isEmpty();
    }

    @Test
    public void getFunctionalMailboxNoFmb() {
        when(repository.findByCode(LDU_CODE)).thenReturn(Optional.of(lduWithNoFmb()));
        assertThat(service.getFunctionalMailbox(LDU_CODE)).isEmpty();
    }

    @Test
    public void saveNewFmb() {
        when(repository.findByCode(LDU_CODE)).thenReturn(Optional.empty());

        service.setFunctionalMailbox(LDU_CODE, FMB);

        verify(repository).save(lduWithFmb());
    }

    @Test
    public void updateFmb() {
        final var ldu = lduWithFmb();

        when(repository.findByCode(LDU_CODE)).thenReturn(Optional.of(ldu));

        service.setFunctionalMailbox(LDU_CODE, FMB2);

        assertThat(ldu.getFunctionalMailbox()).isEqualTo(FMB2);
    }

    static LocalDeliveryUnit lduWithFmb() {
        return LocalDeliveryUnit
                .builder()
                .code(LDU_CODE)
                .functionalMailbox(FMB)
                .build();
    }

    static LocalDeliveryUnit lduWithNoFmb() {
        return LocalDeliveryUnit
                .builder()
                .code(LDU_CODE)
                .build();
    }
}

