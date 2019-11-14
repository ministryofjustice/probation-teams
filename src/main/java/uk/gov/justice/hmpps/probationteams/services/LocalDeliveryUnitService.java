package uk.gov.justice.hmpps.probationteams.services;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit;
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnitRepository;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Service
@Validated
@AllArgsConstructor
@Transactional(readOnly = true)
public class LocalDeliveryUnitService {
    private final LocalDeliveryUnitRepository repository;


    public Optional<LocalDeliveryUnit> getLocalDeliveryUnit(String code) {
        return repository.findByCode(code);
    }

    public Optional<String> getFunctionalMailBox(String localDeliveryUnitCode) {
        return getLocalDeliveryUnit(localDeliveryUnitCode).map(LocalDeliveryUnit::getFunctionalMailBox);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
    public void setFunctionalMailBox(@NotBlank String localDeliveryUnitCode, @Email String proposedFunctionalMailBox) {
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
    public void deleteFunctionalMailBox(@NotBlank String localDeliveryUnitCode) {
    }
}
