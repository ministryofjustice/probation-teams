package uk.gov.justice.hmpps.probationteams.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocalDeliveryUnitRepository extends JpaRepository<LocalDeliveryUnit, UUID> {
    Optional<LocalDeliveryUnit> findByCode(String code);
}
