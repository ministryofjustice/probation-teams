package uk.gov.justice.hmpps.probationteams.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2;
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnit2Repository;

import java.util.Optional;

@Service
@Validated
@AllArgsConstructor
@Transactional(readOnly = true)
public class LocalDeliveryUnit2Service {
    private final LocalDeliveryUnit2Repository repository;

    public Optional<LocalDeliveryUnit2> getLocalDeliveryUnit(String probationAreaCode, String localDeliveryUnitCode) {
        return repository.findByProbationAreaCodeAndLocalDeliveryUnitCode(probationAreaCode, localDeliveryUnitCode);
    }
}
