package uk.gov.justice.hmpps.probationteams.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.probationteams.model.LocalDeliveryUnit2;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocalDeliveryUnit2Repository extends JpaRepository<LocalDeliveryUnit2, UUID> {
    Optional<LocalDeliveryUnit2> findByProbationAreaCodeAndLocalDeliveryUnitCode(String probationAreaCode, String localDeliveryUnitCode);
}
