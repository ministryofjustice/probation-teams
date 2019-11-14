package uk.gov.justice.hmpps.probationteams.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.hmpps.probationteams.config.SecurityUserContext;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit;
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnitRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalDeliveryUnitServiceTest {

    @Mock
    private LocalDeliveryUnitRepository localDeliveryUnitRepository;

//    @Mock
//    private SecurityUserContext securityUserContext;

    private LocalDeliveryUnitService localDeliveryUnitService;

    @Before
    public void setUp() {
        localDeliveryUnitService = new LocalDeliveryUnitService(localDeliveryUnitRepository);
    }

    @Test
    public void getLocalDeliveryUnit() {
        when(localDeliveryUnitRepository.findByCode("ABC123")).thenReturn(Optional.of(LocalDeliveryUnit.builder().code("ABC123").build()));
        final var optLdu = localDeliveryUnitService.getLocalDeliveryUnit("ABC123");
        assertThat(optLdu.isPresent()).isTrue();
    }
}

