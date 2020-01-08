package uk.gov.justice.hmpps.probationteams.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2;
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnit2Repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalDeliveryUnit2ServiceTest {
    private static final String PROBATION_AREA_CODE = "ABC";
    private static final String LDU_CODE = "ABC123";
    private static final String FMB = "a@b.com";
    private static final String FMB2 = "x@y.gov.uk";

    @Mock
    private LocalDeliveryUnit2Repository repository;

    private LocalDeliveryUnit2Service service;

    @Before
    public void setUp() {
        service = new LocalDeliveryUnit2Service(repository);
    }

    @Test
    public void getLocalDeliveryUnit() {

        when(repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE)).thenReturn(Optional.of(lduWithFmb()));
        assertThat(service.getLocalDeliveryUnit(PROBATION_AREA_CODE, LDU_CODE)).isPresent();
    }

    @Test
    public void getLocalDeliveryUnitNoLdu() {
        when(repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(PROBATION_AREA_CODE, LDU_CODE)).thenReturn(Optional.empty());
        assertThat(service.getLocalDeliveryUnit(PROBATION_AREA_CODE, LDU_CODE)).isEmpty();
    }

    static LocalDeliveryUnit2 lduWithFmb() {
        return LocalDeliveryUnit2
                .builder()
                .probationAreaCode(PROBATION_AREA_CODE)
                .localDeliveryUnitCode(LDU_CODE)
                .functionalMailbox(FMB)
                .build();
    }

    static LocalDeliveryUnit2 lduWithNoFmb() {
        return LocalDeliveryUnit2
                .builder()
                .probationAreaCode(PROBATION_AREA_CODE)
                .localDeliveryUnitCode(LDU_CODE)
                .build();
    }
}

