package uk.gov.justice.hmpps.probationteams.services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit;
import uk.gov.justice.hmpps.probationteams.repository.LocalDeliveryUnitRepository;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Optional;

@Service
@Validated
@AllArgsConstructor
@Transactional(readOnly = true)
public class LocalDeliveryUnitService {
    private final LocalDeliveryUnitRepository repository;

    public Page<LocalDeliveryUnit> getLocalDeliveryUnits(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<LocalDeliveryUnit> getLocalDeliveryUnit(String code) {
        return repository.findByCode(code);
    }

    public Optional<String> getFunctionalMailbox(String localDeliveryUnitCode) {
        return getLocalDeliveryUnit(localDeliveryUnitCode)
                .flatMap(ldu -> Optional.ofNullable(ldu.getFunctionalMailbox()));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
    public SetOutcome setFunctionalMailbox(
            @NotBlank @Pattern(regexp = "^[A-Z0-9_]+$", message = "Invalid Local Delivery Unit code") String localDeliveryUnitCode,
            @Email String proposedFunctionalMailbox) {

        return getLocalDeliveryUnit(localDeliveryUnitCode).map(ldu ->
        {
            ldu.setFunctionalMailbox(proposedFunctionalMailbox);
            return SetOutcome.UPDATED;
        }).orElseGet(() ->
        {
            createFunctionalMailbox(localDeliveryUnitCode, proposedFunctionalMailbox);
            return SetOutcome.CREATED;
        });
    }

    private void createFunctionalMailbox(String localDeliveryUnitCode, String functionalMailbox) {
        repository.save(LocalDeliveryUnit.builder().code(localDeliveryUnitCode).functionalMailbox(functionalMailbox).build());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MAINTAIN_REF_DATA', 'SYSTEM_USER')")
    public DeleteOutcome deleteLocalDeliveryUnit(@NotBlank String localDeliveryUnitCode) {
        return repository.findByCode(localDeliveryUnitCode).map(ldu -> {
            repository.delete(ldu);
            return DeleteOutcome.DELETED;
        }).orElse(DeleteOutcome.NOT_FOUND);
    }
}
